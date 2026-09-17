package org.aulune.authentigo.domain
package user

import token.TotpSecret


/** Repository which stores [[User]] objects.
 *  @tparam F effect type.
 */
trait UserRepository[F[_]]:
  /** Persist user in repository.
   *  @param user user to persist.
   *  @return user if success, otherwise the violated [[UserConstraint]].
   */
  def persist(user: User): F[Either[UserConstraint, User]]

  /** Retrieve user by their identity.
   *  @param id user's identity.
   *  @return user if found.
   */
  def get(id: UserId): F[Option[User]]

  /** Finds a user by their unique email.
   *  @param email user's unique email.
   *  @return user if found.
   */
  def getByEmail(email: Email): F[Option[User]]

  /** Updates an existing user's password hash and TOTP secret, but only if
   *  their current TOTP secret still matches `expectedTotpSecret`. This
   *  compare-and-swap guards against a lost update when two concurrent updates
   *  race (e.g. two uses of the same password-reset code).
   *
   *  @param id ID of the user to update.
   *  @param newHashedPassword new password hash.
   *  @param newTotpSecret new TOTP secret; invalidates any outstanding
   *    password-reset codes bound to the old one.
   *  @param expectedTotpSecret TOTP secret the user is expected to currently
   *    have.
   *  @return `true` if the user was updated, `false` if no such user exists or
   *    their current TOTP secret didn't match `expectedTotpSecret`.
   */
  def updatePassword(
      id: UserId,
      newHashedPassword: String,
      newTotpSecret: TotpSecret,
      expectedTotpSecret: TotpSecret,
  ): F[Boolean]
