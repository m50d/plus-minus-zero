package com.github.m50d.plusminuszero

import monix.execution.Scheduler.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._

object EMAClientIT {
  def main(args: Array[String]): Unit = {
    val results = Await.result(EMAClient.currentResultsFor("11990050"), 10 seconds)
    results foreach println
  }
}
