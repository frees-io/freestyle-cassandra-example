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
import freestyle.cassandra.codecs._
import freestyle.cassandra.implicits._
import freestyle.async.implicits._
import freestyle.asyncCatsEffect.implicits._
import freestyle.asyncGuava.implicits._
import freestyle.cassandra.query.interpolator.RuntimeCQLInterpolator._
import freestyle.cassandra.query.interpolator._
import freestyle.cassandra.sample.Implicits._
import freestyle.cassandra.sample.Model._
import freestyle.loggingJVM.implicits._
import monix.eval.Task
import monix.execution.Scheduler

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object StringInterpolator extends App {

  import Modules.CassandraApp
  import DummySchemaInterpolator._

  implicit val executionContext: Scheduler = Scheduler.Implicits.global

  def connect[F[_]](implicit clusterAPI: ClusterAPI[F]): FreeS[F, Session] =
    clusterAPI.connectKeyspace("demodb")

  def closeSession[F[_]](implicit sessionAPI: SessionAPI[F]): FreeS[F, Unit] = sessionAPI.close

  def close[F[_]](implicit clusterAPI: ClusterAPI[F]): FreeS[F, Unit] = clusterAPI.close

  val uuid = UUID.randomUUID()

  implicit def instance[F[_]]: FreeSLift[F, monix.eval.Task] = new FreeSLift[F, monix.eval.Task] {
    def liftFSPar[A](ga: monix.eval.Task[A]): FreeS.Par[F, A] = ga.liftFSPar[F]
  }

  def program(implicit queryModule: QueryModule[QueryModule.Op]): FreeS[QueryModule.Op, User] = {

    implicit val s = queryModule.sessionAPI

    val insertUserTask: FreeS[QueryModule.Op, ResultSet] =
      cql"INSERT INTO users (id, name) VALUES ($uuid, 'Username');".asResultSet()
    val getUserTask: FreeS[QueryModule.Op, ResultSet] =
      cql"SELECT id, name FROM users WHERE id = $uuid".asResultSet()

    for {
      _             <- insertUserTask
      userResultSet <- getUserTask
      user = {
        val userRow = userResultSet.one()
        User(userRow.getUUID(0), userRow.getString(1))
      }
      _ <- queryModule.sessionAPI.close
    } yield user
  }

  val beforeTask: Task[Session] = connect[ClusterAPI.Op].interpret[Task]
  implicit val session: Session = Await.result(beforeTask.runAsync, Duration.Inf)

  val task: Task[User] = program.interpret[Task]
  val user: User = Await.result(task.runAsync, Duration.Inf)
  println("User inserted and fetched")
  println("*************************")
  println(user)
  println("*************************")

  val afterTask: Task[Unit] = close[ClusterAPI.Op].interpret[Task]
  Await.result(afterTask.runAsync, Duration.Inf)

  Await.result(closeSession[SessionAPI.Op].interpret[Task].runAsync, Duration.Inf)
  Await.result(close[ClusterAPI.Op].interpret[Task].runAsync, Duration.Inf)

}
