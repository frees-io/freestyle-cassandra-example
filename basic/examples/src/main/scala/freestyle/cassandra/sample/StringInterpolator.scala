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
import com.datastax.driver.core._
import freestyle._
import freestyle.implicits._
import freestyle.cassandra.api._
import freestyle.cassandra.implicits._
import freestyle.asyncCatsEffect.implicits._
import freestyle.cassandra.sample.Implicits._
import freestyle.cassandra.sample.Model._
import freestyle.loggingJVM.implicits._
import monix.eval.Task
import monix.execution.Scheduler

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import freestyle.cassandra.sample.handlers.implicits._

object StringInterpolator extends App {

  import Modules.StringInterpolatorApp
  implicit val executionContext: Scheduler = Scheduler.Implicits.global

  def connect[F[_]](implicit clusterAPI: ClusterAPI[F]): FreeS[F, Session] =
    clusterAPI.connectKeyspace("demodb")

  def closeSession[F[_]](implicit sessionAPI: SessionAPI[F]): FreeS[F, Unit] = sessionAPI.close

  def close[F[_]](implicit clusterAPI: ClusterAPI[F]): FreeS[F, Unit] = clusterAPI.close

  val uuid = UUID.randomUUID()

  implicit def instance[F[_]]: FreeSLift[F, monix.eval.Task] = new FreeSLift[F, monix.eval.Task] {
    def liftFSPar[A](ga: monix.eval.Task[A]): FreeS.Par[F, A] = ga.liftFSPar[F]
  }

  def program[F[_]](implicit app: StringInterpolatorApp[F]): FreeS[F, User] = {

    implicit val s = app.queryModule.sessionAPI

    for {
      _             <- app.log.debug(s"# Executing insert query with id $uuid")
      _             <- app.userApi.insert(uuid)
      _             <- app.log.debug("# Selecting previous inserted item")
      userResultSet <- app.userApi.get(uuid)
      user = {
        val userRow = userResultSet.one()
        User(userRow.getUUID(0), userRow.getString(1))
      }
      _             <- app.log.debug(s"# Fetched item: $user")
      _             <- app.log.debug(s"# Closing connection")
      _             <- app.queryModule.sessionAPI.close
    } yield user
  }

  val beforeTask: Task[Session] = connect[ClusterAPI.Op].interpret[Task]
  implicit val session: Session = Await.result(beforeTask.runAsync, Duration.Inf)

  val task: Task[User] = program[StringInterpolatorApp.Op].interpret[Task]
  val user: User = Await.result(task.runAsync, Duration.Inf)

  val afterTask: Task[Unit] = close[ClusterAPI.Op].interpret[Task]
  Await.result(afterTask.runAsync, Duration.Inf)

  Await.result(closeSession[SessionAPI.Op].interpret[Task].runAsync, Duration.Inf)
  Await.result(close[ClusterAPI.Op].interpret[Task].runAsync, Duration.Inf)

}
