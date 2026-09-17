package org.aulune.authentigo.domain
package token

/** Six-digit numeric verification code. */
opaque type VerificationCode <: String = String


object VerificationCode:
  private val Pattern = "^[0-9]{6}$".r

  /** Returns a [[VerificationCode]] if given string is a 6-digit numeric code.
   *  @param code code as string.
   */
  def apply(code: String): Option[VerificationCode] =
    Option.when(Pattern.matches(code))(code)

  /** Unsafe constructor to use within always-valid boundary.
   *  @param code code.
   *  @throws IllegalArgumentException if invalid arguments are given.
   */
  def unsafe(code: String): VerificationCode = VerificationCode(code) match
    case Some(value) => value
    case None        => throw new IllegalArgumentException()
