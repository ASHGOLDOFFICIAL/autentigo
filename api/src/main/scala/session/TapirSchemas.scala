package org.aulune.authentigo
package api
package session


import org.aulune.authentigo.application.session.CreateSessionRequest
import org.aulune.authentigo.application.session.Session
import sttp.tapir.Schema


private[session] object TapirSchemas:
  given Schema[CreateSessionRequest] = Schema.derived
  given Schema[Session] = Schema.derived
