package org.aulune.authentigo.domain
package user


import user.User.ValidationResult

import token.TotpSecret

import cats.data.{Validated, ValidatedNec}
import cats.syntax.all.given


/** User representation.
 *  @param id user's unique UUID.
 *  @param email unique email.
 *  @param hashedPassword password hash (if user has it).
 *  @param totpSecret secret used to generate and verify password-reset codes.
 */
final case class User private (
    id: UserId,
    email: Email,
    hashedPassword: Option[String],
    totpSecret: TotpSecret,
):
  /** Copies with validation. */
  def update(
      id: UserId = id,
      email: Email = email,
      hashedPassword: Option[String] = hashedPassword,
      totpSecret: TotpSecret = totpSecret,
  ): ValidationResult[User] = User(
    id = id,
    email = email,
    hashedPassword = hashedPassword,
    totpSecret = totpSecret,
  )


object User:
  private type ValidationResult[A] = ValidatedNec[UserValidationError, A]

  /** Creates user using only required fields. Other fields are initialized as
   *  `None`.
   *  @param id user's UUID.
   *  @param email unique email.
   *  @param totpSecret secret used to generate and verify password-reset codes.
   *  @return user validation result.
   */
  def create(
      id: UserId,
      email: Email,
      totpSecret: TotpSecret,
  ): ValidationResult[User] = User(
    id = id,
    email = email,
    hashedPassword = None,
    totpSecret = totpSecret,
  )

  /** Returns a user if all given arguments are valid.
   *  @param id UUID assigned to user.
   *  @param email unique email.
   *  @return user validation result.
   */
  def apply(
      id: UserId,
      email: Email,
      hashedPassword: Option[String],
      totpSecret: TotpSecret,
  ): ValidationResult[User] = validateState(
    new User(
      id = id,
      email = email,
      hashedPassword = hashedPassword,
      totpSecret = totpSecret,
    ))

  /** Unsafe constructor for always valid boundary.
   *  @throws UserValidationError if arguments are invalid.
   */
  def unsafe(
      id: UserId,
      email: Email,
      hashedPassword: Option[String],
      totpSecret: TotpSecret,
  ): User = User(
    id = id,
    email = email,
    hashedPassword = hashedPassword,
    totpSecret = totpSecret,
  ) match
    case Validated.Valid(a)   => a
    case Validated.Invalid(e) => throw e.head

  /** Validates object state.
   *  @param user user to validate.
   *  @return validation result.
   */
  private def validateState(user: User): ValidationResult[User] = user.validNec
