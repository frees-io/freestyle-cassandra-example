package freestyle.cassandra.sample

import freestyle._
import freestyle.cassandra.api._
import freestyle.cassandra.sample.algebras.UserAPI
import freestyle.logging._
import freestyle.implicits._
import freestyle.cassandra.implicits._

object Modules {

  @module trait CassandraApp {
    val queryModule: QueryModule
    val log: LoggingM
  }

  @module trait StringInterpolatorApp {
    val queryModule: QueryModule
    val log: LoggingM
    val userApi: UserAPI
  }

}
