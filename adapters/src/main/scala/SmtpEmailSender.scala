package org.aulune.authentigo
package adapters


import domain.user.Email

import cats.effect.Sync
import cats.syntax.all.*
import com.comcast.ip4s.Host
import com.comcast.ip4s.Port
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport

import java.util.Properties


/** [[EmailSender]] implementation using SMTP. */
object SmtpEmailSender:
  /** Builds the service.
   *  @param host SMTP host.
   *  @param port SMTP port.
   *  @param username SMTP auth username.
   *  @param password SMTP auth password.
   *  @param fromAddress address emails are sent from.
   *  @tparam F effect type.
   */
  def build[F[_]: Sync](
      host: Host,
      port: Port,
      username: String,
      password: String,
      fromAddress: Email,
  ): F[EmailSender[F]] =
    new SmtpEmailSender[F](host, port, username, password, fromAddress).pure[F]

  private final class PasswordAuthenticator(username: String, password: String)
      extends Authenticator:
    override def getPasswordAuthentication: PasswordAuthentication =
      new PasswordAuthentication(username, password)
end SmtpEmailSender


private final class SmtpEmailSender[F[_]: Sync](
    host: Host,
    port: Port,
    username: String,
    password: String,
    fromAddress: Email,
) extends EmailSender[F]:

  override def send(to: Email, subject: String, body: String): F[Unit] =
    Sync[F].blocking {
      val message = new MimeMessage(session)
      message.setFrom(new InternetAddress(fromAddress))
      message.setRecipients(Message.RecipientType.TO, to)
      message.setSubject(subject)
      message.setText(body)
      Transport.send(message)
    }

  private val session = Session.getInstance(
    buildProperties(),
    SmtpEmailSender.PasswordAuthenticator(username, password),
  )

  private def buildProperties(): Properties =
    val properties = new Properties()
    properties.put("mail.smtp.host", host.toString)
    properties.put("mail.smtp.port", port.value.toString)
    properties.put("mail.smtp.auth", "true")
    properties.put("mail.smtp.starttls.enable", "true")
    properties
