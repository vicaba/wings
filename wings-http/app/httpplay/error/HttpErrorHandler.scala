package httpplay.error

import scala.concurrent.Future

import play.api.mvc.{Controller, Result}

import wings.toolkit.error.application.Types.AppError

import org.scalactic._


trait HttpErrorHandler {

  def handle[T](or: Or[T, Every[AppError]])(success: T => Result): Result

  def handleAsync[T](or: Or[T, Every[AppError]])(success: T => Future[Result]): Future[Result]

}

class DefaultHttpErrorHandler extends HttpErrorHandler with Controller {

  implicit val appErrorWriteable = AppErrorWriteable.appErrorWriteable

  override def handle[T](or: Or[T, Every[AppError]])(success: (T) => Result): Result =
    or match {
      case Good(t) => success(t)
      case Bad(b)  => whenBad(b)
    }

  override def handleAsync[T](or: Or[T, Every[AppError]])(success: (T) => Future[Result]): Future[Result] =
    or match {
      case Good(t) => success(t)
      case Bad(b)  => Future.successful(whenBad(b))
    }

  def whenBad(bad: Every[AppError]): Result = bad match {
    case One(one)             => BadRequest(one.message)
    case many: Many[AppError] => BadRequest(many.firstElement)
  }

}
