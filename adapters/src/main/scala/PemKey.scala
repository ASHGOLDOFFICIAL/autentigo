package org.aulune.authentigo
package adapters

/** PEM-encoded key material. */
opaque type PemKey <: String = String


object PemKey:
  def apply(raw: String): PemKey = raw.replace("\\n", "\n")
