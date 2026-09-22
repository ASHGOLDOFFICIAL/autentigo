package org.aulune.authentigo
package adapters
package session


import domain.token.AccessTokenPayload
import domain.token.IdTokenPayload
import domain.token.RefreshTokenPayload
import domain.token.TokenString
import domain.user.Email
import domain.user.User
import domain.user.UserId

import cats.Monad
import cats.data.OptionT
import cats.effect.Clock
import cats.effect.Sync
import cats.syntax.all.given
import com.nimbusds.jose.jwk.JWK
import io.circe.parser.decode
import io.circe.syntax.given
import io.circe.Decoder
import io.circe.Encoder
import org.typelevel.log4cats.Logger.optionTLogger
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.LoggerFactory
import pdi.jwt.JwtAlgorithm
import pdi.jwt.JwtCirce
import pdi.jwt.JwtClaim
import pdi.jwt.JwtOptions

import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.time.Instant
import scala.concurrent.duration.FiniteDuration


/** Implementation of [[AccessTokenService]], [[IdTokenService]] and
 *  [[RefreshTokenService]].
 *  @param issuer what to put in `iss`.
 *  @param privateKey EC private key used to sign tokens.
 *  @param publicKey EC public key used to verify tokens (the counterpart of
 *    `privateKey`).
 *  @param accessExpiration expiration time for access and ID tokens.
 *  @param refreshExpiration expiration time for refresh tokens.
 *  @tparam F effect type.
 */
final class JwtTokenService[F[_]: Monad: Clock: LoggerFactory](
    issuer: String,
    privateKey: ECPrivateKey,
    publicKey: ECPublicKey,
    accessExpiration: FiniteDuration,
    refreshExpiration: FiniteDuration,
) extends AccessTokenService[F]
    with IdTokenService[F]
    with RefreshTokenService[F]:

  private given Logger[F] = LoggerFactory[F].getLogger

  // Expiration checks are disable to do them manually
  private val options = JwtOptions(expiration = false)
  private val algo = JwtAlgorithm.ES256

  override def generateAccessToken(user: User): F[TokenString] =
    Clock[F].realTimeInstant.map { now =>
      val payload = makeAccessTokenPayload(user, now, maxAccessExp)
      val claim = JwtCirce.encode(payload.asJson, privateKey, algo)
      // It shouldn't be empty, otherwise it's exceptional situation.
      TokenString.unsafe(claim)
    }

  /** Makes [[AccessTokenPayload]] for given values. */
  private def makeAccessTokenPayload(
      user: User,
      now: Instant,
      maxExp: Long,
  ): AccessTokenPayload =
    val iat = now.getEpochSecond
    val exp = iat + maxExp
    AccessTokenPayload(
      iss = issuer,
      sub = user.id,
      exp = exp,
      iat = iat,
    )

  override def generateIdToken(user: User): F[TokenString] =
    Clock[F].realTimeInstant.map { now =>
      val payload = makeIdTokenPayload(user, now)
      val claim = JwtCirce.encode(payload.asJson, privateKey, algo)
      // It shouldn't be empty, otherwise it's exceptional situation.
      TokenString.unsafe(claim)
    }

  /** Makes [[IdTokenPayload]] for given values. */
  private def makeIdTokenPayload(user: User, now: Instant): IdTokenPayload =
    val iat = now.getEpochSecond
    val exp = iat + maxAccessExp
    IdTokenPayload(
      iss = issuer,
      sub = user.id,
      aud = "?", // TODO: fix
      exp = exp,
      iat = iat,
      email = user.email,
    )

  override def generateRefreshToken(user: User): F[TokenString] =
    Clock[F].realTimeInstant.map { now =>
      val payload = makeRefreshTokenPayload(user, now)
      val claim = JwtCirce.encode(payload.asJson, privateKey, algo)
      // It shouldn't be empty, otherwise it's exceptional situation.
      TokenString.unsafe(claim)
    }

  /** Makes [[RefreshTokenPayload]] for given values. */
  private def makeRefreshTokenPayload(
      user: User,
      now: Instant,
  ): RefreshTokenPayload =
    val iat = now.getEpochSecond
    val exp = iat + maxRefreshExp
    RefreshTokenPayload(iss = issuer, sub = user.id, exp = exp, iat = iat)

  override def decodeAccessToken(
      token: TokenString,
  ): F[Option[UserId]] =
    decodeSubject[AccessTokenPayload](token, maxAccessExp, _.exp, _.sub)

  override def decodeRefreshToken(
      token: TokenString,
  ): F[Option[UserId]] =
    decodeSubject[RefreshTokenPayload](token, maxRefreshExp, _.exp, _.sub)

  /** Decodes given token's subject, checking its expiration against `maxExp`.
   *  @param token token to decode.
   *  @param maxExp maximum allowed time-to-live for this kind of token.
   *  @param getExp extracts `exp` claim from payload.
   *  @param getSub extracts `sub` claim from payload.
   */
  private def decodeSubject[Payload: Decoder](
      token: TokenString,
      maxExp: Long,
      getExp: Payload => Long,
      getSub: Payload => UserId,
  ): F[Option[UserId]] = (for
    _ <- optionTLogger.info(s"Decoding token: $token")
    claim <- decodeClaim(token).toOptionT
    payload <- decode[Payload](claim.toJson).toOption.toOptionT
    expirationValid <-
      OptionT.liftF(validateExpiration(getExp(payload), maxExp))
    id <- OptionT.when(expirationValid)(getSub(payload))
  yield id).value

  /** Returns claim if token is successfully decoded.
   *  @param token token.
   */
  private def decodeClaim(token: TokenString): Option[JwtClaim] = JwtCirce
    .decode(token, publicKey, Seq(algo), options)
    .toOption

  /** Validates the expiration claim against the current time.
   *
   *  A token is considered valid if:
   *    - its expiration timestamp is after the current time (i.e. not expired),
   *      and
   *    - its expiration timestamp is not too far in the future, based on given
   *      `maxExp`.
   *
   *  @param exp token `exp` claim.
   *  @param maxExp maximum allowed time-to-live for this kind of token.
   *  @return `true` if the token is valid, `false` otherwise.
   */
  private def validateExpiration(exp: Long, maxExp: Long): F[Boolean] =
    Clock[F].realTimeInstant.map { now =>
      now.getEpochSecond < exp && exp < maxAllowed(now, maxExp)
    }

  private val maxAccessExp = accessExpiration.toSeconds
  private val maxRefreshExp = refreshExpiration.toSeconds

  /** Maximum allowed instant of time for a token's `exp` field.
   *  @param now current timestamp.
   *  @param maxExp maximum allowed time-to-live for this kind of token.
   */
  private def maxAllowed(now: Instant, maxExp: Long) =
    now.plusSeconds(maxExp).getEpochSecond

  private given Encoder[AccessTokenPayload] = Encoder.derived
  private given Decoder[AccessTokenPayload] = Decoder.derived
  private given Encoder[IdTokenPayload] = Encoder.derived
  private given Encoder[RefreshTokenPayload] = Encoder.derived
  private given Decoder[RefreshTokenPayload] = Decoder.derived

  private given Decoder[UserId] = Decoder.decodeUUID.map(UserId.apply)
  private given Encoder[UserId] = Encoder.encodeUUID.contramap(identity)
  private given Encoder[Email] = Encoder.encodeString.contramap(identity)


object JwtTokenService:
  /** Builds an instance from PEM-encoded EC keys.
   *  @param issuer what to put in `iss`.
   *  @param privateKeyPem PEM-encoded EC private key (PKCS8).
   *  @param publicKeyPem PEM-encoded EC public key (SPKI).
   *  @param accessExpiration expiration time for access and ID tokens.
   *  @param refreshExpiration expiration time for refresh tokens.
   *  @tparam F effect type.
   *  @throws IllegalArgumentException if either PEM can't be parsed as an EC
   *    key.
   */
  def build[F[_]: Sync: Clock: LoggerFactory](
      issuer: String,
      privateKeyPem: PemKey,
      publicKeyPem: PemKey,
      accessExpiration: FiniteDuration,
      refreshExpiration: FiniteDuration,
  ): F[JwtTokenService[F]] = Sync[F].delay {
    val privateKey =
      JWK.parseFromPEMEncodedObjects(privateKeyPem).toECKey.toECPrivateKey
    val publicKey =
      JWK.parseFromPEMEncodedObjects(publicKeyPem).toECKey.toECPublicKey
    new JwtTokenService[F](
      issuer,
      privateKey,
      publicKey,
      accessExpiration,
      refreshExpiration,
    )
  }
