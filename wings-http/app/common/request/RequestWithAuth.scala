package common.request

import java.util.UUID

import scala.concurrent.Future

import play.api.libs.concurrent.{Execution => playEc}
import play.api.mvc.{ActionBuilder, Request, WrappedRequest, _}

import wings.enrichments.UUIDHelper
import wings.user.infrastructure.keys.UserKeys

/**
  * Request that can be authenticated
  * @param request   original request
  * @tparam A request parser
  */
class CanBeAuthenticatedRequest[A](val request: Request[A]) extends WrappedRequest[A](request)

/**
  * Unauthenticated request
  * @param request   original request
  * @tparam A request parser
  */
class UnauthenticatedRequest[A](override val request: Request[A]) extends CanBeAuthenticatedRequest(request)

/**
  * Authenticated request
  * @param user the user name
  * @param userid the user id
  * @param request   original request
  * @tparam A request parser
  */
class AuthenticatedRequest[A](val user: String, userid: UUID, override val request: Request[A])
    extends CanBeAuthenticatedRequest[A](request)

/**
  * The user must be authenticated
  */
object AuthenticatedAction extends ActionBuilder[AuthenticatedRequest] with Controller {
  override def invokeBlock[A](request: Request[A],
                              block: (AuthenticatedRequest[A]) => Future[Result]): Future[Result] = {
    // Check if the user is authenticated
    sessionAuthenticate(request).fold {
      Future {
        Unauthorized("")
      }(playEc.defaultContext)
    } { case (name, uuid) => block(new AuthenticatedRequest[A](name, uuid, request)) }
  }

  /**
    * Looks if the user is authenticated (session only)
    * @param request the request
    * @return
    */
  def sessionAuthenticate(request: RequestHeader): Option[(String, UUID)] = {
    request.session
      .get(UserKeys.NameKey)
      .flatMap(name => request.session.get(UserKeys.IdKey).map((name, _)))
      .flatMap { case (name, uid) => UUIDHelper.tryFromString(uid).map(uuid => (name, uuid)).toOption }
  }
}

/**
  * The user can be authenticated
  */
object CanBeAuthenticatedAction extends ActionBuilder[CanBeAuthenticatedRequest] {

  def invokeBlock[A](request: Request[A], block: (CanBeAuthenticatedRequest[A]) => Future[Result]): Future[Result] = {
    AuthenticatedAction
      .sessionAuthenticate(request)
      .fold {
        block(new UnauthenticatedRequest[A](request))
      } {
        case (name, uuid) =>
          block(new AuthenticatedRequest[A](name, uuid, request))
      }
  }

  /**
    * Helper object
    */
  object Fold {

    private def partialFunctionBuilder[T](authenticated: (AuthenticatedRequest[_]) => T)(
        unauthenticated: (UnauthenticatedRequest[_]) => T): PartialFunction[CanBeAuthenticatedRequest[_], T] = {
      case ar: AuthenticatedRequest[_]   => authenticated(ar)
      case ur: UnauthenticatedRequest[_] => unauthenticated(ur)
    }

    /**
      * Fold over authenticated or unauthenticated action
      * @param request  the request
      * @param authenticated  the function to apply for authenticated requests
      * @param unauthenticated  the function to apply for unauthenticated requests
      * @tparam T The result type of the functions (function composition)
      * @return the type T
      */
    def apply[T](request: CanBeAuthenticatedRequest[_])(authenticated: (AuthenticatedRequest[_]) => T)(
        unauthenticated: (UnauthenticatedRequest[_]) => T): T = {
      partialFunctionBuilder(authenticated)(unauthenticated)(request)
    }

  }

}
