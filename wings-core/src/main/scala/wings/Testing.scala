package wings

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import scaldi.Injectable._
import wings.config.DependencyInjector._
import wings.virtualobjectagent.domain.messages.event.repository.VirtualObjectSensedRepository

object Testing {

  def main(args: Array[String]): Unit = {

    val virtualObjectSensedRepository: VirtualObjectSensedRepository =
      inject[VirtualObjectSensedRepository](identified by 'VirtualObjectSensedRepository)

    Thread.sleep(400)

    val f = virtualObjectSensedRepository.findAll().map { list =>
      println(list)

    }

    f.recover {
      case t: Throwable => println(t)
    }

    println(f)

    Await.ready(f, 5 seconds)

  }

}
