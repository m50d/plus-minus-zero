package com.github.m50d.plusminuszero

import com.softwaremill.sttp._
import monix.execution.Scheduler
import org.scalajs.dom.{DOMParser, Node}
import org.scalajs.dom.ext._

object EMAClient {
  implicit val sttpBackend = FetchBackend()

  def currentResultsFor(id: String)(implicit scheduler: Scheduler) =
    sttp.get(Uri(s"http://mahjong-europe.org/ranking/Players/$id.html")).send() map {
      response ⇒
        val document = new DOMParser().parseFromString(response.body.right.get, response.header("Content-Type").get)
        val resultTable = document.getElementsByTagName("h3").find {
          n: Node ⇒ n.textContent == "Riichi Results"
        }.get.nextSibling.nextSibling.nextSibling
        val body = resultTable.childNodes.filter(_.nodeName == "tbody").apply(1)
        body.childNodes.map {
          row ⇒
            val points = row.childNodes(6).childNodes(0).textContent.stripSuffix(" pts").toInt
            val originalWeight = row.childNodes(4).textContent.replace(',', '.').toDouble
            val multiplier = row.childNodes(8).childNodes(0).textContent match {
              case "100%" ⇒ 1.0
              case "50%" ⇒ 0.5
            }
            val label = row.childNodes(3).childNodes(0).textContent
            TournamentResult(originalWeight * multiplier, points, Some(label))
        } toVector
    }
}
