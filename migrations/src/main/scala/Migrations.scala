package org.aulune.migrations


import cats.effect.Sync
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor

import java.sql.DriverManager


/** Applies Liquibase changelogs against a JDBC database. */
object Migrations:
  /** Applies all pending changes from the given changelog.
   *  @param jdbcUrl JDBC connection URL.
   *  @param user database user.
   *  @param password database password.
   *  @param changelogPath classpath-relative path to the Liquibase changelog.
   *  @tparam F effect type.
   */
  def run[F[_]: Sync](
      jdbcUrl: String,
      user: String,
      password: String,
      changelogPath: String,
  ): F[Unit] = Sync[F].blocking {
    val connection = DriverManager.getConnection(jdbcUrl, user, password)
    try
      val database = DatabaseFactory
        .getInstance()
        .findCorrectDatabaseImplementation(new JdbcConnection(connection))
      new Liquibase(changelogPath, new ClassLoaderResourceAccessor(), database)
        .update()
    finally connection.close()
  }
