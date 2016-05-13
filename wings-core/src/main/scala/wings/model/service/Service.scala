package wings.model.service

trait Service

object Services {

  case class Repository() extends Service

  def MainRepository = Repository()
}
