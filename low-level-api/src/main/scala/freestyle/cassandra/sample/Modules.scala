package freestyle.cassandra.sample

import freestyle._
import freestyle.cassandra.api.{SessionAPI, StatementAPI}
import freestyle.logging._

object Modules {

  @module trait CassandraApp {
    val sessionAPI: SessionAPI
    val statementAPI: StatementAPI
    val log: LoggingM
  }

}
