package org.aulune.authentigo.domain
package token

import user.UserId


/** Refresh token payload.
 *
 *  @param iss Issuer Identifier, should be HTTPS URI of our backend.
 *  @param sub Subject Identifier, should be user's unique ID.
 *  @param exp expiration time on or after which the refresh token should be
 *    considered invalid.
 *  @param iat time at which the JWT was issued.
 */
final case class RefreshTokenPayload(
    iss: String,
    sub: UserId,
    exp: Long,
    iat: Long,
)
