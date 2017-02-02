package wings.config

import com.typesafe.config.ConfigFactory

object Config {

  lazy val config: com.typesafe.config.Config = ConfigFactory.load(Config.Environment)

  val DevEnv: String = "dev"

  val ProdEnv: String = "prod"

  val Environment: String = DevEnv

}
