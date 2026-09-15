package org.aulune.authentigo
package adapters
package session


import domain.token.{IdTokenPayload, TokenString}
import domain.user.User


/** Service that generates ID tokens. ID token payload should be
 *  [[IdTokenPayload]].
 *
 *  Token type, generation and validation rules depend on implementation.
 *
 *  @tparam F effect type.
 */
trait IdTokenService[F[_]]:
  /** Generates ID token for given user.
   *  @param user user for whom to generate ID token.
   */
  def generateIdToken(user: User): F[TokenString]
