package org.aulune.authentigo
package adapters
package user


import domain.user.{Email, User, UserConstraint, UserId}

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.aulune.commons.testing.PostgresTestContainer
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers


/** Tests for [[PostgresUserRepository]]. */
final class PostgresUserRepositoryTest
    extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with PostgresTestContainer:

  private def stand = makeStand(PostgresUserRepository.build[IO])

  private val testUser = User.unsafe(
    id = UserId.unsafe("7690e9ab-700d-46ef-9e46-2bcce2d56ae3"),
    email = Email.unsafe("user@example.com"),
    hashedPassword = Option("test_hash"),
  )

  "get method " - {
    "should " - {
      "return `None` for non-existent user" in stand { repo =>
        for user <- repo.get(testUser.id)
        yield user shouldBe None
      }

      "retrieve existing users" in stand { repo =>
        for
          _ <- repo.persist(testUser)
          user <- repo.get(testUser.id)
        yield user shouldBe Some(testUser)
      }
    }
  }

  "persist method " - {
    "should " - {
      "persist a new user" in stand { repo =>
        for result <- repo.persist(testUser)
        yield result shouldBe Right(testUser)
      }

      "return the violated constraint if a user exists" in stand { repo =>
        val updatedTestUser = testUser
          .update(email = Email.unsafe("new_user@example.com"))
          .getOrElse(throw new IllegalStateException())
        for
          _ <- repo.persist(testUser)
          result <- repo.persist(updatedTestUser)
        yield result shouldBe Left(UserConstraint.UniqueId)
      }

      "return the violated constraint when adding user with taken email" in stand {
        repo =>
          val another = testUser
            .update(id = UserId.unsafe("eab28102-2ecd-4ff2-8572-58143fbe920d"))
            .getOrElse(throw new IllegalStateException())
          for
            _ <- repo.persist(testUser)
            result <- repo.persist(another)
          yield result shouldBe Left(UserConstraint.UniqueEmail)
      }
    }
  }

  "getByEmail method " - {
    "should " - {
      "return `None` for non-existent user" in stand { repo =>
        for user <- repo.getByEmail(testUser.email)
        yield user shouldBe None
      }

      "retrieve existing users" in stand { repo =>
        for
          _ <- repo.persist(testUser)
          user <- repo.getByEmail(testUser.email)
        yield user shouldBe Some(testUser)
      }
    }
  }

end PostgresUserRepositoryTest
