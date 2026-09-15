package org.aulune.authentigo.domain
package user


import java.util.UUID
import scala.util.Try


/** Unique identifier of a [[User]]. */
opaque type UserId <: UUID = UUID


object UserId:
  /** Returns a [[UserId]] from given UUID. */
  def apply(uuid: UUID): UserId = uuid

  /** Validates string to be a valid [[UserId]].
   *  @param uuid UUID as string.
   */
  def apply(uuid: String): Option[UserId] = Try(UUID.fromString(uuid)).toOption

  /** Unsafe constructor to use inside always-valid boundary.
   *  @param uuid UUID as string.
   *  @throws IllegalArgumentException if given string isn't a valid UUID.
   */
  def unsafe(uuid: String): UserId = UUID.fromString(uuid)
