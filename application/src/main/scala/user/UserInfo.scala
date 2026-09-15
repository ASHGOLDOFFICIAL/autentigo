package org.aulune.authentigo.application
package user

import java.util.UUID


/** Representation of user that can be used to identify user.
 *  @param id user's unique ID.
 *  @param email email.
 */
final case class UserInfo(
    id: UUID,
    email: String,
)
