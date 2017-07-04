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
import com.datastax.driver.core.{BoundStatement, Session}
import freestyle._
import freestyle.cassandra.codecs._
import freestyle.cassandra.sample.Implicits._
import freestyle.cassandra.sample.Model._
import freestyle.cassandra.api.{ClusterAPI, LowLevelAPI}
import monix.eval.{Task => MonixTask}
import monix.cats._
import monix.execution.Scheduler

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Program extends App {

  def connect[F[_]](implicit clusterAPI: ClusterAPI[F]): FreeS[F, Session] =
    clusterAPI.connectKeyspace("demodb")

  def program[F[_]](implicit lowLevelAPI: LowLevelAPI[F]): FreeS[F, User] = {

    val newUser = User(UUID.randomUUID(), "Username")

    def prepareStatement(
        implicit c1: ByteBufferCodec[UUID],
        c2: ByteBufferCodec[String]): FreeS[F, BoundStatement] =
      lowLevelAPI.prepare("INSERT INTO User (id, name) VALUES (?, ?)") map { st =>
        val boundStatement = st.bind()
        boundStatement.setBytesUnsafe("id", c1.serialize(newUser.id))
        boundStatement.setBytesUnsafe("name", c2.serialize(newUser.name))
        boundStatement
      }

    for {
      statement <- prepareStatement
      _         <- lowLevelAPI.executeStatement(statement)
      user <- lowLevelAPI.executeWithMap(
        s"SELECT id, name FROM User WHERE id = ?",
        Map("id" -> newUser.id)) map { rs =>
        val row = rs.one()
        User(row.getUUID(0), row.getString(1))
      }
      _ <- lowLevelAPI.close
    } yield user
  }

  def close[F[_]](implicit clusterAPI: ClusterAPI[F]): FreeS[F, Unit] = clusterAPI.close
  
  implicit val executionContext: Scheduler = Scheduler.Implicits.global

  val beforeTask: MonixTask[Session] = connect[ClusterAPI.Op].interpret[MonixTask]

  implicit val session: Session = Await.result(beforeTask.runAsync, Duration.Inf)

  val task: MonixTask[User] = program[LowLevelAPI.Op].interpret[MonixTask]

  val result: User = Await.result(task.runAsync, Duration.Inf)
  println(result)

  val afterTask: MonixTask[Unit] = close[ClusterAPI.Op].interpret[MonixTask]
  Await.result(afterTask.runAsync, Duration.Inf)

}
