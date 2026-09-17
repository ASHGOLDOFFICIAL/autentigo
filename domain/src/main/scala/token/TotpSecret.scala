package org.aulune.authentigo.domain
package token

/** Secret used to generate and verify [[VerificationCode]]s. */
opaque type TotpSecret <: String = String


object TotpSecret:
  /** Returns a [[TotpSecret]] if given string is non-empty.
   *  @param secret secret as string.
   */
  def apply(secret: String): Option[TotpSecret] =
    Option.when(secret.nonEmpty)(secret)

  /** Unsafe constructor to use within always-valid boundary.
   *  @param secret secret.
   *  @throws IllegalArgumentException if invalid arguments are given.
   */
  def unsafe(secret: String): TotpSecret = apply(secret) match
    case Some(value) => value
    case None        => throw new IllegalArgumentException()
