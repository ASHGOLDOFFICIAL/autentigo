package org.aulune.authentigo
package adapters
package session


import domain.token.TokenString
import domain.user.User
import domain.user.UserId


/** Service that generates and decodes refresh tokens.
 *
 *  Token type, generation and validation rules depend on implementation.
 *  @tparam F effect type.
 */
trait RefreshTokenService[F[_]]:
  /** Returns ID of the user whom this refresh token identifies.
   *  @param token token as string.
   */
  def decodeRefreshToken(token: TokenString): F[Option[UserId]]

  /** Generates a refresh token for given user.
   *  @param user user for whom to generate a refresh token.
   */
  def generateRefreshToken(user: User): F[TokenString]
