package org.aulune.authentigo
package api

import io.circe.generic.extras.Configuration

/** Circe configuration for API. */
given baseConfig: Configuration = Configuration.default.withSnakeCaseMemberNames
