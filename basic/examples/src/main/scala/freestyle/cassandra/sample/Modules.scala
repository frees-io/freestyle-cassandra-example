package freestyle.cassandra.sample

import freestyle._
import freestyle.cassandra.api._
import freestyle.logging._

object Modules {

  @module trait CassandraApp {
    val queryModule: QueryModule
    val log: LoggingM
  }

}
