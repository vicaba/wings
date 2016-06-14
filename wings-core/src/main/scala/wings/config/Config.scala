package wings.config

import com.typesafe.config.ConfigFactory

object Config {

  lazy val config = ConfigFactory.load("general")

}
