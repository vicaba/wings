package httpplay.config

import httpplay.error.{DefaultHttpErrorHandler, HttpErrorHandler}
import scaldi.Module


object DependencyInjector {

  val _httpInjector = new Module {

    bind[HttpErrorHandler] identifiedBy 'HttpErrorHandler to new DefaultHttpErrorHandler

  }

  implicit val httpInjector =  wings.config.DependencyInjector.coreInjector :: _httpInjector
}
