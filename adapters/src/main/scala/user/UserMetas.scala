package org.aulune.authentigo
package adapters
package user


import domain.user.Email

import doobie.Meta


/** [[Meta]] instances for user object. */
private[user] object UserMetas:
  given Meta[Email] = Meta[String].imap(Email.unsafe)(identity)
