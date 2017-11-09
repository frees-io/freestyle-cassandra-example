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
import com.datastax.driver.core.{BoundStatement, PreparedStatement, Session}
import freestyle._
import freestyle.implicits._
import freestyle.cassandra.api.ClusterAPI
import freestyle.cassandra.codecs._
import freestyle.cassandra.sample.Implicits._
import freestyle.cassandra.sample.Model._
import freestyle.loggingJVM.implicits._
import monix.eval.{Task => MonixTask}
import monix.execution.Scheduler

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object LowLevelApi extends App {

  import Modules.CassandraApp

  def connect[F[_]](implicit clusterAPI: ClusterAPI[F]): FreeS[F, Session] =
    clusterAPI.connectKeyspace("demodb")

  def program[F[_]](implicit app: CassandraApp[F]): FreeS[F, User] = {

    import app._

    val newUser = User(UUID.randomUUID(), "Username")

    def bindValues(st: PreparedStatement)(
        implicit c1: ByteBufferCodec[UUID],
        c2: ByteBufferCodec[String]): FreeS[F, BoundStatement] =
      List(
        statementAPI.setValueByName(_: BoundStatement, "id", newUser.id, c1),
        statementAPI.setValueByName(_: BoundStatement, "name", newUser.name, c2))
        .foldLeft[FreeS[F, BoundStatement]](statementAPI.bind(st)) { (freeS, func) =>
          freeS.flatMap(boundSt => func(boundSt))
        }

    for {
      _ <- log.debug("# Preparing insert query")
      preparedStatement <- sessionAPI.prepare("INSERT INTO users (id, name) VALUES (?, ?)")
      _ <- log.debug("# Binding values")
      boundStatement    <- bindValues(preparedStatement)
      _ <- log.debug(s"# Executing insert query with id ${newUser.id}")
      _                 <- sessionAPI.executeStatement(boundStatement)
      _ <- log.debug("# Selecting previous inserted item")
      user <- sessionAPI.executeWithMap(
        s"SELECT id, name FROM users WHERE id = ?",
        Map("id" -> newUser.id)) map { rs =>
        val row = rs.one()
        User(row.getUUID(0), row.getString(1))
      }
      _ <- log.debug(s"# Fetched item $user")
      _ <- log.debug(s"# Closing connection")
      _ <- sessionAPI.close
    } yield user

  }

  def close[F[_]](implicit clusterAPI: ClusterAPI[F]): FreeS[F, Unit] = clusterAPI.close

  implicit val executionContext: Scheduler = Scheduler.Implicits.global

  val beforeTask: MonixTask[Session] = connect[ClusterAPI.Op].interpret[MonixTask]
  implicit val session: Session = Await.result(beforeTask.runAsync, Duration.Inf)

  val task: MonixTask[User] = program[CassandraApp.Op].interpret[MonixTask]
  Await.result(task.runAsync, Duration.Inf)

  val afterTask: MonixTask[Unit] = close[ClusterAPI.Op].interpret[MonixTask]
  Await.result(afterTask.runAsync, Duration.Inf)

}
