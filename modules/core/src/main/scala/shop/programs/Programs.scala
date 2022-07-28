package shop.programs

import retry.RetryPolicies._
import cats.syntax.all._
import scala.concurrent.duration._

object Programs {

  // val retryPolicy = limitRetries[F](3) |+| exponentialBackoff(10.milliseconds)
}
