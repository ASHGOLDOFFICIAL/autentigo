package org.aulune.authentigo
package adapters

import domain.user.Email


/** Sends emails.
 *  @tparam F effect type.
 */
trait EmailSender[F[_]]:
  /** Sends a plain-text email.
   *  @param to recipient address.
   *  @param subject email subject.
   *  @param body plain-text email body.
   */
  def send(to: Email, subject: String, body: String): F[Unit]
