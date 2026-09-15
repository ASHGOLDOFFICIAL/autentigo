package org.aulune.authentigo.application
package session


/** A created session's tokens.
 *  @param accessToken access token.
 *  @param idToken ID token.
 *  @param refreshToken refresh token, used to obtain a new session without
 *    re-authenticating.
 */
final case class Session(
    accessToken: String,
    idToken: String,
    refreshToken: String,
)
