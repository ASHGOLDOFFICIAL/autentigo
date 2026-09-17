package org.aulune.authentigo
package api
package session


import org.aulune.authentigo.application.session.CreateSessionRequest.BasicAuthentication
import org.aulune.authentigo.application.session.CreateSessionRequest
import org.aulune.authentigo.application.session.Session
import sttp.tapir.EndpointIO.Example


private[session] object Examples:
  private val BasicAuthenticationExample: CreateSessionRequest =
    BasicAuthentication(
      email = "user@example.com",
      password = "password",
    )

  val CreateSessionRequestExamples: List[Example[CreateSessionRequest]] = List(
    Example(
      value = BasicAuthenticationExample,
      name = Some("Basic authentication."),
      summary = Some("Basic authentication with email and password."),
    ),
  )

  val SessionExample: Session = Session(
    accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
      "eyJpc3MiOiJodHRwczovL2V4YW1wbGUub3JnIiwic3ViIjoiMTIzNCIsImV4cCI6MTAwMDA2MCwiaWF0IjoxMDAwMDAwLCJncm91cHMiOlsiYWRtaW4iXX0." +
      "FXLb53Syf8jrOhbdiPQaqQObuH-FUtAypTEJPmmChZo",
    idToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
      "eyJpc3MiOiJodHRwczovL2V4YW1wbGUub3JnIiwic3ViIjoiMTIzNCIsImF1ZCI6Imh0dHBzOi8vZXhhbXBsZS5vcmciLCJleHAiOjEwMDAwNjAsImlhdCI6MTAwMDAwMCwidXNlcm5hbWUiOiJhZG1pbiJ9." +
      "i1a7tAK9chVZLUZ-KIb9P-lO1ff7o_ATR7jhzSypZfk",
    refreshToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
      "eyJpc3MiOiJodHRwczovL2V4YW1wbGUub3JnIiwic3ViIjoiMTIzNCIsImV4cCI6MTAwMDA2MCwiaWF0IjoxMDAwMDAwfQ." +
      "9U3jH6PZ7hRZQ1Z1u1JYQK1Wf1k1i6Z1v8bR6q4c7iA",
  )
