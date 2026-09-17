package org.aulune.authentigo
package adapters
package session


import domain.token.AccessTokenPayload
import domain.token.TokenString
import domain.user.User
import domain.user.UserId


/** Service that generates and decodes access tokens. Access token payload
 *  should be [[AccessTokenPayload]].
 *
 *  Token type, generation and validation rules depend on implementation.
 *  @tparam F effect type.
 */
trait AccessTokenService[F[_]]:
  /** Returns ID of the user whom this token identifies.
   *  @param token token as string.
   */
  def decodeAccessToken(token: TokenString): F[Option[UserId]]

  /** Generates access token for given user.
   *  @param user user for whom to generate access token.
   */
  def generateAccessToken(user: User): F[TokenString]
