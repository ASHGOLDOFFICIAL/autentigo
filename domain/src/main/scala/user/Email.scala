package org.aulune.authentigo.domain
package user

import com.sanctionco.jmail.JMail

/** Email address, validated via [[JMail]]. */
opaque type Email <: String = String


object Email:
  private val validator = JMail.strictValidator()

  /** Returns an email if given string is a valid email address.
   *  @param email email as string.
   */
  def apply(email: String): Option[Email] =
    Option.when(validator.isValid(email))(email)

  /** Unsafe constructor to use within always-valid boundary.
   *  @param email email.
   *  @throws IllegalArgumentException if invalid arguments are given.
   */
  def unsafe(email: String): Email = apply(email) match
    case Some(value) => value
    case None        => throw new IllegalArgumentException()
