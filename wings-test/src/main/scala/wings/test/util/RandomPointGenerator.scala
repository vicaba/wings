package wings.test.util

import play.api.libs.json.{JsObject, Json}


object RandomPointGenerator {

  case class Point(latitude: Double, longitude: Double)

  /**
    * Grabbed from <a>https://gist.github.com/vicaba/98fca2dbfdc0417ad5542aa14d23afab</a>
    * @param center
    * @param radius
    * @return
    */
  def randomPoint(center: Point, radius: Double): Point = {
    val (x0, y0) = Point.unapply(center).get

    val rd = radius/111300

    val u = Math.random()
    val v = Math.random()

    val w = rd * Math.sqrt(u)
    val t = 2 * Math.PI * v
    val x = w * Math.cos(t)
    val y = w * Math.sin(t)

    val xp = x / Math.cos(y0)

    Point(xp +x0 , y + y0)
  }

  def geoJsonPoint(latitude: Double, longitude: Double): JsObject =
  Json.obj(
    "geometry" -> Json.obj(
      "type" -> "Point",
      "coordinates" -> List(latitude, longitude)
    )
  )

  def generateRandomJsonPoint(): JsObject = {
    val p = RandomPointGenerator.randomPoint(Point(41.406358, 2.158722), 3000)
    geoJsonPoint(p.latitude, p.longitude)
  }

  def main(args: Array[String]): Unit = {
    println(generateRandomJsonPoint())
  }

}




