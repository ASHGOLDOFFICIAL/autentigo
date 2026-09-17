package org.aulune.authentigo
package adapters


import domain.token.{TotpSecret, VerificationCode}

import cats.effect.{Clock, Sync}
import cats.syntax.all.given
import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator

import java.util.Base64
import javax.crypto.spec.SecretKeySpec
import javax.crypto.{KeyGenerator, Mac, SecretKey}
import scala.concurrent.duration.FiniteDuration
import scala.jdk.DurationConverters.*


/** [[VerificationCodeService]] implementation using TOTP.
 *  @param codeExpiration width of each time window a code is valid for.
 *  @tparam F effect type.
 */
final class TotpVerificationCodeService[F[_]: Sync: Clock](
    codeExpiration: FiniteDuration,
) extends VerificationCodeService[F]:

  private val Algorithm = "HmacSHA256"
  private val totp =
    new TimeBasedOneTimePasswordGenerator(codeExpiration.toJava, 6, Algorithm)

  override def generateSecret: F[TotpSecret] = Sync[F].delay {
    val keyGenerator = KeyGenerator.getInstance(Algorithm)
    val macLengthInBits = Mac.getInstance(Algorithm).getMacLength * 8
    keyGenerator.init(macLengthInBits)
    val secret = Base64.getEncoder
      .encodeToString(keyGenerator.generateKey().getEncoded)
    TotpSecret.unsafe(secret)
  }

  override def generateCode(secret: TotpSecret): F[VerificationCode] =
    for
      now <- Clock[F].realTimeInstant
      key = decodeKey(secret)
      code <- Sync[F].blocking(totp.generateOneTimePasswordString(key, now))
    yield VerificationCode.unsafe(code)

  override def verifyCode(
      secret: TotpSecret,
      code: VerificationCode,
  ): F[Boolean] =
    for
      now <- Clock[F].realTimeInstant
      key = decodeKey(secret)
      prev = now.minus(codeExpiration.toJava)
      result <- Sync[F].blocking {
        totp.validateOneTimePassword(key, now, code) ||
        totp.validateOneTimePassword(key, prev, code)
      }
    yield result

  private def decodeKey(secret: TotpSecret): SecretKey =
    new SecretKeySpec(Base64.getDecoder.decode(secret), Algorithm)
