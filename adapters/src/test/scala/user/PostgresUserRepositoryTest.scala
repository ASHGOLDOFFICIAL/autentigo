package org.aulune.authentigo
package adapters
package user


import domain.token.TotpSecret
import domain.user.Email
import domain.user.User
import domain.user.UserConstraint
import domain.user.UserId

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
    totpSecret = TotpSecret.unsafe("totp_secret"),
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

  "updatePassword method " - {
    "should " - {
      "update the password and TOTP secret when the expected secret matches" in stand {
        repo =>
          val newSecret = TotpSecret.unsafe("new_totp_secret")
          val expectedUser = testUser
            .update(hashedPassword = Some("new_hash"), totpSecret = newSecret)
            .getOrElse(throw new IllegalStateException())
          for
            _ <- repo.persist(testUser)
            result <- repo.updatePassword(
              testUser.id,
              "new_hash",
              newSecret,
              testUser.totpSecret,
            )
            user <- repo.get(testUser.id)
          yield (result, user) shouldBe (true, Some(expectedUser))
      }

      "return `false` when the expected secret doesn't match" in stand { repo =>
        for
          _ <- repo.persist(testUser)
          result <- repo.updatePassword(
            testUser.id,
            "new_hash",
            TotpSecret.unsafe("new_totp_secret"),
            TotpSecret.unsafe("wrong_secret"),
          )
          user <- repo.get(testUser.id)
        yield (result, user) shouldBe (false, Some(testUser))
      }

      "return `false` for a non-existent user" in stand { repo =>
        for result <- repo.updatePassword(
            testUser.id,
            "new_hash",
            TotpSecret.unsafe("new_totp_secret"),
            testUser.totpSecret,
          )
        yield result shouldBe false
      }
    }
  }

end PostgresUserRepositoryTest
