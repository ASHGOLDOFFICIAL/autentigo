package org.aulune.authentigo
package adapters
package session


import domain.user.Email
import domain.user.User
import domain.user.UserRepository

import cats.Monad
import cats.data.OptionT
import cats.syntax.all.given


/** Service that manages authentication via email and passwords.
 *  @param repo [[UserRepository]] implementation.
 *  @param hasher password hasher.
 *  @tparam F effect type.
 */
final class BasicAuthenticationHandlerImpl[F[_]: Monad](
    repo: UserRepository[F],
    hasher: PasswordHasher[F],
) extends BasicAuthenticationHandler[F]:

  override def authenticate(
      email: Email,
      password: String,
  ): F[Option[User]] = (for
    user <- OptionT(repo.getByEmail(email))
    _ <- verifyPassword(user, password)
  yield user).value

  /** Return `Unit` if given password is user's password.
   *  @param user user whose password will be checked.
   *  @param password plain password to check.
   */
  private def verifyPassword(user: User, password: String): OptionT[F, Unit] =
    val passwordMatch = user.hashedPassword
      .traverse {
        hasher.verifyPassword(password, _)
      }
      .map(_.getOrElse(false))
    OptionT.whenM(passwordMatch)(().pure[F])
