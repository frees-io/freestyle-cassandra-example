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

import cats.{MonadError, ~>}
import com.datastax.driver.core._
import freestyle.async.implicits._
import monix.eval.Task
import freestyle.asyncCatsEffect.implicits._
import monix.eval.instances.CatsAsyncInstances._
import monix.execution.Scheduler.Implicits.global
import cats.implicits._

object Implicits {

  implicit val cluster: Cluster = Cluster
    .builder()
    .addContactPoint("127.0.0.1")
    .build()

  implicit val stringTypeCodec: TypeCodec[String] = TypeCodec.varchar()

  implicit val uuidTypeCodec: TypeCodec[UUID] = TypeCodec.uuid()

  implicit val protocolVersion: ProtocolVersion = ProtocolVersion.V4

  import freestyle.cassandra.api._
  import freestyle.cassandra.handlers.implicits._

  implicit def clusterAPIInterpreter(implicit cluster: Cluster, E: MonadError[Task, Throwable]): ClusterAPI.Op ~> Task =
    clusterAPIHandler[Task] andThen apiInterpreter[Task, Cluster](cluster)

  implicit def sessionAPIInterpreter(implicit session: Session, E: MonadError[Task, Throwable]): SessionAPI.Op ~> Task =
    sessionAPIHandler[Task] andThen apiInterpreter[Task, Session](session)

  implicit def statementAPIInterpreter(implicit E: MonadError[Task, Throwable]): StatementAPI.Handler[Task] =
    statementAPIHandler[Task]

}
