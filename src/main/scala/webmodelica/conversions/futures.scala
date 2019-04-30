package webmodelica.conversions

/** Enables conversion between ScalaFutures & TwitterFutures by extending SFuture with `asTwitter` and TFuture with `asScala`.
  * Taken from: https://twitter.github.io/util/guide/util-cookbook/futures.html#conversions-between-twitter-s-future-and-scala-s-future.
  */
object futures {
  import com.twitter.util.{Future => TwitterFuture, Promise => TwitterPromise, Return, Throw}
  import scala.concurrent.{Future => ScalaFuture, Promise => ScalaPromise, ExecutionContext}
  import scala.util.{Success, Failure}
  import cats.Monad

  /** Convert from a Twitter Future to a Scala Future */
  implicit class RichTwitterFuture[A](val tf: TwitterFuture[A]) extends AnyVal {
    def asScala(implicit e: ExecutionContext): ScalaFuture[A] = {
      val promise: ScalaPromise[A] = ScalaPromise()
      tf.respond {
        case Return(value) => promise.success(value)
        case Throw(exception) => promise.failure(exception)
      }
      promise.future
    }
  }

  /** Convert from a Scala Future to a Twitter Future */
  implicit class RichScalaFuture[A](val sf: ScalaFuture[A]) extends AnyVal {
    def asTwitter(implicit e: ExecutionContext): TwitterFuture[A] = {
      val promise: TwitterPromise[A] = new TwitterPromise[A]()
      sf.onComplete {
        case Success(value) => promise.setValue(value)
        case Failure(exception) => promise.setException(exception)
      }
      promise
    }
  }

  implicit val twitterFutureMonad = new Monad[TwitterFuture]() {
    override def flatMap[A, B](fa: TwitterFuture[A])(f: A => TwitterFuture[B]): TwitterFuture[B] = fa.flatMap(f)
    override def pure[A](a: A): TwitterFuture[A] = TwitterFuture.value(a)

    override def tailRecM[A, B](init: A)(fn: A => TwitterFuture[Either[A, B]]): TwitterFuture[B] =
      fn(init) flatMap {
        case Right(b) => pure(b)
        case Left(a) => tailRecM(a)(fn)
      }
  }

  def eitherToFuture[A](e:Either[Throwable, A]): TwitterFuture[A] = e match {
    case Left(ex) => TwitterFuture.exception(ex)
    case Right(v) => TwitterFuture.value(v)
  }
}
