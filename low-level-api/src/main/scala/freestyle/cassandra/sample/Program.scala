/*
 * Copyright 2017 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package freestyle.cassandra.sample

import java.util.UUID

import cats.instances.future._
import freestyle._
import freestyle.cassandra.sample.Implicits._
import freestyle.cassandra.sample.Model._
import freestyle.cassandra.api.LowLevelAPI
import monix.eval.{Task => MonixTask}
import monix.cats._
import monix.execution.Scheduler

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.control.NonFatal

object Program extends App {

  implicit val executionContext: Scheduler = Scheduler.Implicits.global

  def program[F[_]](implicit lowLevelAPI: LowLevelAPI[F]): FreeS[F, User] = {

    val newUser = User(UUID.randomUUID(), "Username")

    for {
      _ <- lowLevelAPI.executeStatement(newUser.boundedInsert)
      user <- lowLevelAPI.executeWithMap(
        s"SELECT id, name FROM User WHERE id = ?",
        Map("id" -> newUser.id)) map { rs =>
        val row = rs.one()
        User(row.getUUID(0), row.getString(1))
      }
      _ <- lowLevelAPI.close
    } yield user
  }

  val task: MonixTask[Option[User]] = program[LowLevelAPI.Op]
    .interpret[MonixTask]
    .map(Option(_))
    .onErrorRecover {
      case NonFatal(t) =>
        t.printStackTrace()
        cluster.close()
        None
    }

  val result: Option[User] = Await.result(task.runAsync, Duration.Inf)
  println(result)

  cluster.close()
}
