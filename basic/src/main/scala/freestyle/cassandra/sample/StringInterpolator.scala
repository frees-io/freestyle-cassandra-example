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
import com.datastax.driver.core.{ProtocolVersion, ResultSet, Session, TypeCodec}
import freestyle._
import freestyle.FreeS
import freestyle.async.implicits._
import freestyle.asyncMonix.implicits._
import freestyle.cassandra.api.{ClusterAPI, SessionAPI}
import freestyle.cassandra.codecs._
import freestyle.cassandra.query.interpolator._
import freestyle.cassandra.query.interpolator.RuntimeCQLInterpolator._
import freestyle.cassandra.sample.Implicits._
import freestyle.implicits._
import monix.cats._
import monix.eval.{Task => MonixTask}
import monix.execution.Scheduler

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.control.NonFatal

object StringInterpolator extends App {

  implicit val executionContext: Scheduler = Scheduler.Implicits.global

  def connect[F[_]](implicit clusterAPI: ClusterAPI[F]): FreeS[F, Session] =
    clusterAPI.connectKeyspace("demodb")

  def closeSession[F[_]](implicit sessionAPI: SessionAPI[F]): FreeS[F, Unit] = sessionAPI.close

  def close[F[_]](implicit clusterAPI: ClusterAPI[F]): FreeS[F, Unit] = clusterAPI.close

  implicit val session: Session =
    Await.result(connect[ClusterAPI.Op].interpret[MonixTask].runAsync, Duration.Inf)

  val uuid = java.util.UUID.fromString("f61e816f-f59e-4c0d-b610-a59418eedaa0")

  implicit val uuidCodec: ByteBufferCodec[UUID] =
    freestyle.cassandra.codecs.byteBufferCodec(TypeCodec.uuid(), ProtocolVersion.V5)

  val task: MonixTask[ResultSet] = cql"SELECT id, name FROM users WHERE id = $uuid".asResultSet[MonixTask]
  val resultSet: ResultSet       = Await.result(task.runAsync, Duration.Inf)

  try {
    resultSet.iterator().asScala.foreach { row =>
      println(s"########## User ${row.get[java.util.UUID](0, TypeCodec.uuid()).toString}")
    }
  } catch {
    case NonFatal(t) => t.printStackTrace()
  }

  Await.result(closeSession[SessionAPI.Op].interpret[MonixTask].runAsync, Duration.Inf)

  Await.result(close[ClusterAPI.Op].interpret[MonixTask].runAsync, Duration.Inf)

}
