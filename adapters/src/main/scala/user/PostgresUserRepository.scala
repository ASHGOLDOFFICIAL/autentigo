package org.aulune.authentigo
package adapters
package user


import UserMetas.given
import domain.token.TotpSecret
import domain.user.{Email, User, UserConstraint, UserId, UserRepository}

import cats.effect.MonadCancelThrow
import cats.syntax.all.given
import doobie.Meta
import doobie.Transactor
import doobie.implicits.toSqlInterpolator
import doobie.postgres.implicits.UuidType
import doobie.syntax.all.given
import org.postgresql.util.PSQLException

import java.util.UUID


/** [[UserRepository]] implementation via PostgreSQL. */
object PostgresUserRepository:
  /** Builds an instance.
   *  @param transactor [[Transactor]] instance.
   *  @tparam F effect type.
   */
  def build[F[_]: MonadCancelThrow](
      transactor: Transactor[F],
  ): F[UserRepository[F]] = new PostgresUserRepository[F](transactor).pure[F]

  private val constraintMap = Map(
    "users_unique_id" -> UserConstraint.UniqueId,
    "users_unique_email" -> UserConstraint.UniqueEmail,
  )

  /** If `e` is a unique-violation error for one of `constraintMap`'s
   *  constraints, returns the violated [[UserConstraint]].
   *  @param e error to inspect.
   */
  private def violatedConstraint(e: Throwable): Option[UserConstraint] = e match
    case e: PSQLException => Option(e.getServerErrorMessage)
        .flatMap(m => Option(m.getConstraint))
        .flatMap(constraintMap.get)
    case _ => None

end PostgresUserRepository


private final class PostgresUserRepository[F[_]: MonadCancelThrow](
    transactor: Transactor[F],
) extends UserRepository[F]:

  private given Meta[UserId] = Meta[UUID].imap(UserId.apply)(identity)

  override def persist(user: User): F[Either[UserConstraint, User]] = sql"""
      |INSERT INTO autentigo.users (id, email, password, totp_secret)
      |VALUES (
      |  ${user.id},
      |  ${user.email},
      |  ${user.hashedPassword},
      |  ${user.totpSecret}
      |)""".stripMargin.update.run
    .as(user)
    .transact(transactor)
    .attempt
    .flatMap {
      case Right(u) => u.asRight[UserConstraint].pure[F]
      case Left(e)  => PostgresUserRepository.violatedConstraint(e) match
          case Some(c) => c.asLeft[User].pure[F]
          case None    => e.raiseError
    }

  override def get(id: UserId): F[Option[User]] = sql"""
      |SELECT id, email, password, totp_secret
      |FROM autentigo.users
      |WHERE id = $id""".stripMargin
    .query[SelectResult]
    .map(toUser)
    .option
    .transact(transactor)

  override def getByEmail(email: Email): F[Option[User]] = sql"""
    |SELECT id, email, password, totp_secret
    |FROM autentigo.users
    |WHERE email = $email""".stripMargin
    .query[SelectResult]
    .map(toUser)
    .option
    .transact(transactor)

  override def updatePassword(
      id: UserId,
      newHashedPassword: String,
      newTotpSecret: TotpSecret,
      expectedTotpSecret: TotpSecret,
  ): F[Boolean] = sql"""
      |UPDATE autentigo.users
      |SET password = $newHashedPassword, totp_secret = $newTotpSecret
      |WHERE id = $id AND totp_secret = $expectedTotpSecret
      |""".stripMargin.update.run
    .transact(transactor)
    .map(_ > 0)

  private type SelectResult = (
      UserId,
      Email,
      Option[String],
      TotpSecret,
  )

  /** Makes users from given data. */
  private def toUser(
      id: UserId,
      email: Email,
      password: Option[String],
      totpSecret: TotpSecret,
  ) = User.unsafe(
    id = id,
    email = email,
    hashedPassword = password,
    totpSecret = totpSecret)
