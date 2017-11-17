package freestyle.cassandra.sample.handlers

import java.util.UUID

import cats.MonadError
import com.datastax.driver.core.{ResultSet, Session}
import freestyle.async.AsyncContext
import freestyle.cassandra.sample.Model.SchemaInterpolator
import freestyle.cassandra.sample.algebras.UserAPI
import freestyle.cassandra.sample.Implicits._
import freestyle.cassandra.query.interpolator._
import freestyle.cassandra.api._

import scala.concurrent.ExecutionContext

object implicits {

  implicit def userApiHandler[F[_]](implicit API: SessionAPI[SessionAPI.Op],
    S: Session,
    AC: AsyncContext[F],
    E: ExecutionContext,
    ME: MonadError[F, Throwable]): UserAPI.Handler[F] = new UserAPI.Handler[F] {

    import SchemaInterpolator._

    override protected[this] def insert(userId: UUID): F[ResultSet] =
      cql"INSERT INTO demodb.users (id, name) VALUES ($userId, 'Username');".attemptResultSet[F]()

    override protected[this] def get(userId: UUID): F[ResultSet] =
      cql"SELECT id, name FROM demodb.users WHERE id = $userId".attemptResultSet[F]()
  }

}
