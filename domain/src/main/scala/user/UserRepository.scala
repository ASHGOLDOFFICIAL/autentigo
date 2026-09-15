package org.aulune.authentigo.domain
package user


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
