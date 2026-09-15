package org.aulune.authentigo.domain
package user


/** Constraints that exist on users as collection. */
enum UserConstraint:
  /** ID should be unique. */
  case UniqueId

  /** Email should be unique. */
  case UniqueEmail
