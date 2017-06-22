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

import cats.~>
import com.datastax.driver.core.{Cluster, ProtocolVersion, Session, TypeCodec}
import freestyle.asyncMonix.implicits._
import freestyle.cassandra.api.LowLevelAPI
import monix.eval.{Task => MonixTask}


object Implicits {

  val cluster: Cluster = Cluster
    .builder()
    .addContactPoint("127.0.0.1")
    .build()

  lazy implicit val session: Session = cluster.connect("demodb")

  implicit val stringTypeCodec: TypeCodec[String] = TypeCodec.varchar()

  implicit val uuidTypeCodec: TypeCodec[UUID] = TypeCodec.uuid()

  implicit val protocolVersion: ProtocolVersion = ProtocolVersion.V4

  import freestyle.cassandra.implicits._

  implicit val lowLevelAPIHandler: LowLevelAPI.Op ~> MonixTask = listenableFutureHandler andThen listenableFuture2Async[MonixTask]

}
