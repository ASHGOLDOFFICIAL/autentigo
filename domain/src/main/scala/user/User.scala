package org.aulune.authentigo.domain
package user


import user.User.ValidationResult

import cats.data.{Validated, ValidatedNec}
import cats.syntax.all.given


/** User representation.
 *  @param id user's unique UUID.
 *  @param email unique email.
 *  @param hashedPassword password hash (if user has it).
 */
final case class User private (
    id: UserId,
    email: Email,
    hashedPassword: Option[String],
):
  /** Copies with validation. */
  def update(
      id: UserId = id,
      email: Email = email,
      hashedPassword: Option[String] = hashedPassword,
  ): ValidationResult[User] = User(
    id = id,
    email = email,
    hashedPassword = hashedPassword,
  )


object User:
  private type ValidationResult[A] = ValidatedNec[UserValidationError, A]

  /** Creates user using only required fields. Other fields are initialized as
   *  `None`.
   *  @param id user's UUID.
   *  @param email unique email.
   *  @return user validation result.
   */
  def create(
      id: UserId,
      email: Email,
  ): ValidationResult[User] = User(
    id = id,
    email = email,
    hashedPassword = None,
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
  ): ValidationResult[User] = validateState(
    new User(id = id, email = email, hashedPassword = hashedPassword))

  /** Unsafe constructor for always valid boundary.
   *  @throws UserValidationError if arguments are invalid.
   */
  def unsafe(
      id: UserId,
      email: Email,
      hashedPassword: Option[String],
  ): User = User(id = id, email = email, hashedPassword = hashedPassword) match
    case Validated.Valid(a)   => a
    case Validated.Invalid(e) => throw e.head

  /** Validates object state.
   *  @param user user to validate.
   *  @return validation result.
   */
  private def validateState(user: User): ValidationResult[User] = user.validNec
