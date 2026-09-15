package org.aulune.authentigo.application
package session


/** Request to create a new session (log in). */
enum CreateSessionRequest:
  /** Authentication via email and password.
   *  @param email email of user trying to log in.
   *  @param password user's password.
   */
  case BasicAuthentication(email: String, password: String)
