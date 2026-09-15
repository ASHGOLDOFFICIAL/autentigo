package org.aulune.authentigo
package api
package user


import CirceCodecs.given
import Examples.{CreateUserRequestExample, UserInfoExample}
import TapirSchemas.given
import org.aulune.authentigo.application.user.{
  CreateUserRequest,
  UserInfo,
  UserService,
}
import org.aulune.commons.errors.adapters.circe.ErrorResponseCodecs.given
import org.aulune.commons.errors.adapters.tapir.ErrorResponseSchemas.given
import org.aulune.commons.errors.adapters.tapir.ErrorStatusCodeMapper
import org.aulune.commons.errors.ErrorResponse

import cats.Functor
import cats.syntax.all.given
import sttp.model.StatusCode
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.{auth, endpoint, path, statusCode, stringToPath}


/** Controller with Tapir endpoints for the `users` resource.
 *  @param service [[UserService]] to use.
 *  @tparam F effect type.
 */
final class UserController[F[_]: Functor](
    service: UserService[F],
):
  private val collection = "users"
  private val tag = "Users"

  private val createUserEndpoint = endpoint.post
    .in(collection)
    .in(jsonBody[CreateUserRequest]
      .description("Registration details.")
      .example(CreateUserRequestExample))
    .out(statusCode(StatusCode.Ok).and(jsonBody[UserInfo]
      .description("Created user.")
      .example(UserInfoExample)))
    .errorOut(statusCode.and(jsonBody[ErrorResponse]))
    .name("CreateUser")
    .summary("Register a new user.")
    .tag(tag)
    .serverLogic { request =>
      for result <- service.createUser(request)
      yield result.leftMap(ErrorStatusCodeMapper.toApiResponse)
    }

  private val getUserEndpoint = endpoint.get
    .in(collection / path[String]("user"))
    .in(auth.bearer[String]())
    .out(statusCode(StatusCode.Ok).and(jsonBody[UserInfo]
      .description("Requested user.")
      .example(UserInfoExample)))
    .errorOut(statusCode.and(jsonBody[ErrorResponse]))
    .name("GetUser")
    .summary("Get a user. Restricted to the authenticated user themselves.")
    .tag(tag)
    .serverLogic { case (id, accessToken) =>
      for result <- service.getUser(id, accessToken)
      yield result.leftMap(ErrorStatusCodeMapper.toApiResponse)
    }

  /** Returns Tapir endpoints for the `users` resource. */
  def endpoints: List[ServerEndpoint[Any, F]] = List(
    createUserEndpoint,
    getUserEndpoint,
  )
