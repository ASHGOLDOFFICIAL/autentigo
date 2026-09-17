package org.aulune.authentigo
package api
package session


import CirceCodecs.given
import Examples.CreateSessionRequestExamples
import Examples.SessionExample
import TapirSchemas.given
import org.aulune.authentigo.application.session.CreateSessionRequest
import org.aulune.authentigo.application.session.Session
import org.aulune.authentigo.application.session.SessionService
import org.aulune.commons.errors.adapters.circe.ErrorResponseCodecs.given
import org.aulune.commons.errors.adapters.tapir.ErrorResponseSchemas.given
import org.aulune.commons.errors.adapters.tapir.ErrorStatusCodeMapper
import org.aulune.commons.errors.ErrorResponse

import cats.Functor
import cats.syntax.all.given
import sttp.model.StatusCode
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.auth
import sttp.tapir.endpoint
import sttp.tapir.statusCode
import sttp.tapir.stringToPath


/** Controller with Tapir endpoints for the `sessions` resource.
 *  @param service [[SessionService]] to use.
 *  @tparam F effect type.
 */
final class SessionController[F[_]: Functor](
    service: SessionService[F],
):
  private val collection = "sessions"
  private val tag = "Sessions"

  private val createSessionEndpoint = endpoint.post
    .in(collection)
    .in(
      jsonBody[CreateSessionRequest]
        .description("Login information.")
        .examples(CreateSessionRequestExamples),
    )
    .out(
      statusCode(StatusCode.Ok).and(
        jsonBody[Session]
          .description("Tokens to use in calls to API.")
          .example(SessionExample),
      ),
    )
    .errorOut(statusCode.and(jsonBody[ErrorResponse]))
    .name("CreateSession")
    .summary("Create a session (log in) to receive tokens.")
    .tag(tag)
    .serverLogic { request =>
      for result <- service.createSession(request)
      yield result.leftMap(ErrorStatusCodeMapper.toApiResponse)
    }

  private val refreshSessionEndpoint = endpoint.post
    .in(collection + ":refresh")
    .in(auth.bearer[String]())
    .out(
      statusCode(StatusCode.Ok).and(
        jsonBody[Session]
          .description("New tokens.")
          .example(SessionExample),
      ),
    )
    .errorOut(statusCode.and(jsonBody[ErrorResponse]))
    .name("RefreshSession")
    .summary("Exchange a refresh token for a new session.")
    .tag(tag)
    .serverLogic { refreshToken =>
      for result <- service.refreshSession(refreshToken)
      yield result.leftMap(ErrorStatusCodeMapper.toApiResponse)
    }

  /** Returns Tapir endpoints for the `sessions` resource. */
  def endpoints: List[ServerEndpoint[Any, F]] = List(
    createSessionEndpoint,
    refreshSessionEndpoint,
  )
