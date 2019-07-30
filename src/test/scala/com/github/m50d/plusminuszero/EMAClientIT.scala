package com.github.m50d.plusminuszero

import com.softwaremill.sttp._

object EMAClientIT {
  def main(args: Array[String]): Unit = {
    implicit val backend = HttpURLConnectionBackend()
    val results = EMAClient.currentResultsFor("11990050", true)
    results foreach println
  }
}
