package org.aulune.authentigo
package adapters
package user


import domain.token.TotpSecret
import domain.user.Email

import doobie.Meta


/** [[Meta]] instances for user object. */
private[user] object UserMetas:
  given Meta[Email] = Meta[String].imap(Email.unsafe)(identity)
  given Meta[TotpSecret] = Meta[String].imap(TotpSecret.unsafe)(identity)
