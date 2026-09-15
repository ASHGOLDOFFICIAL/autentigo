package org.aulune.authentigo.application
package user

/** Request to register a new user.
 *  @param email desired email.
 *  @param password plain-text password.
 */
final case class CreateUserRequest(email: String, password: String)
