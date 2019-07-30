package com.github.m50d.plusminuszero

import cats.Functor
import cats.syntax.functor._
import com.softwaremill.sttp._
import org.scalajs.dom.{DOMParser, Node}
import org.scalajs.dom.ext._
import org.scalajs.dom.raw.{HTMLTableCellElement, HTMLTableRowElement}
import slogging.StrictLogging

object EMAClient extends StrictLogging {
  def currentResultsFor[R[_] : Functor](id: String, addXRequestedWith: Boolean)(implicit backend: SttpBackend[R, _]) = {
    val request = sttp.get(Uri("cors-anywhere.herokuapp.com").path(
      "http:", "", "mahjong-europe.org", "ranking", "Players", s"$id.html")
    )
    val requestWithHeader = if (addXRequestedWith) request.header("X-Requested-With", "XMLHttpRequest") else request
    requestWithHeader.send() map {
      response ⇒
        val document = new DOMParser().parseFromString(response.body.right.get, response.header("Content-Type").get)
        logger.info(s"Fetched document: $document")
        val resultTable = document.getElementsByTagName("h3").find {
          n: Node ⇒ n.lastChild.textContent == "Riichi Results"
        }.get.nextSibling.nextSibling.nextSibling.nextSibling.nextSibling.nextSibling
        logger.info(s"Result table: $resultTable ${resultTable.textContent}")
        val bodies = resultTable.childNodes.filter { node ⇒
          logger.info(s"Node name: ${node.nodeName}")
          node.nodeName equalsIgnoreCase "tbody"
        }.drop(1)
        logger.info(s"Rows: $bodies")
        bodies.map {
          body ⇒
            val cells = body.childNodes.collectFirst {
              case tr: HTMLTableRowElement ⇒ tr.childNodes.collect {
                case td: HTMLTableCellElement ⇒ td.textContent.trim
              }
            }.get
            logger.info(s"Cells: $cells")
            val pointsTxt = cells(6).stripSuffix(" pts")
            logger.info(s"PointsTxt: $pointsTxt")
            val points = pointsTxt.toInt
            val originalWeight = cells(4).replace(',', '.').toDouble
            val multiplier = cells(8) match {
              case "100%" ⇒ 1.0
              case "50%" ⇒ 0.5
            }
            val label = cells(3)
            val res = TournamentResult(originalWeight * multiplier, points, Some(label))
            logger.info(s"Result: $res")
            res
        } toVector
    }
  }
}
