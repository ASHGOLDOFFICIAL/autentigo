package org.aulune.authentigo
package adapters
package session

import domain.user.{Email, User}


/** Service that manages basic authentication via email and password.
 *
 *  @tparam F effect type.
 */
trait BasicAuthenticationHandler[F[_]]:
  /** Returns user if authentication is successful, otherwise `None`.
   *  @param email email.
   *  @param password raw password.
   */
  def authenticate(email: Email, password: String): F[Option[User]]
