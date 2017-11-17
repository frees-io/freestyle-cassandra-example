package freestyle.cassandra.sample.algebras

import java.util.UUID

import com.datastax.driver.core.ResultSet
import freestyle.free

@free
trait UserAPI {

  def insert(userId: UUID): FS[ResultSet]

  def get(userId: UUID): FS[ResultSet]
}



