package com.github.m50d.plusminuszero

import cats.syntax.apply._

import scala.scalajs.js.annotation._
import us.oyanglul.owlet._
import org.scalajs.dom._
import DOM._
import fr.hmil.roshttp.{HttpRequest, Protocol}
import monix.reactive.Observable
import monix.reactive.subjects.Var
import monix.execution.Scheduler.Implicits.global

@JSExportTopLevel("plusMinusZero.PlusMinusZero") object PlusMinusZero {
  @JSExport def main(args: Array[String]): Unit = {
    val emaId = Var(None): Var[Option[String]]
    val emaResults = emaId mapFuture (
      id ⇒ HttpRequest(s"http://mahjong-europe.org/ranking/Players/$id.html").send()) map {
      response ⇒
        val responseDocument = new DOMParser().parseFromString(response.body, response.headers("Content-Type"))
    }
    val toAdd = Var(None): Var[Option[TournamentResult]]
    val added = toAdd.scan(Vector[TournamentResult]())(_ ++ _)
    val toRemove = Var(None): Var[Option[TournamentResult]]
    val removed = toRemove.scan(Vector[TournamentResult]())(_ ++ _)
    val listOfResults = added.combineLatestMap(removed)(_ diff _)

    val explanation = {
      val el: html.Paragraph = document.createElement("p").asInstanceOf[html.Paragraph]
      val text = document.createTextNode(
        """
In "weight", enter MERS weight for tournaments in the past year and MERS weight/2
for tournaments between 1 and 2 years ago.

In "points", enter EMA points for your place in the tournament - 1000 for first
and around 0 for last.
""")
      el.appendChild(text)
      Owlet(Observable(List(el)), Var(()))
    }
    val emaIdEntry = label(string("emaid", ""), "EMA ID")
    val importEma = (emaIdEntry, button("import", false, true)).mapN((id, pressed) =>
      emaId := (if(pressed) Some(id) else None)
    )

    val resultEntry = div(TournamentResult.tournamentResult, Var(Seq("result-entry")))
    val addNewResult = (resultEntry, button("add", false, true)).mapN(((entry, pressed) =>
      toAdd := (if (pressed) Some(entry) else None)))

    val rankUi = {
      val sink = Var(())
      val el: html.Div = document.createElement("div").asInstanceOf[html.Div]
      el.className = "tournament-results"
      listOfResults foreach { results =>
        while (el.lastChild != null) {
          el.removeChild(el.lastChild)
        }
        results foreach { result =>
          val rel = document.createElement("div").asInstanceOf[html.Div]
          val tn = document.createTextNode(result.toString)
          rel.className = "tournament-result"
          rel.appendChild(tn)
          val removeButton = document.createElement("button").asInstanceOf[html.Button]
          removeButton.appendChild(document.createTextNode("×"))
          removeButton.onclick = { _ =>
            toRemove := Some(result)
          }
          rel.appendChild(removeButton)
          el.appendChild(rel)
        }
        val score = Rank.ranking(results.toVector)
        val scoreTn = document.createTextNode(s"EMA Ranking points: $score")
        el.appendChild(scoreTn)
        sink := (())
      }
      Owlet(Observable(List(el)), sink)
    }
    render(explanation *> importEma *> addNewResult *> rankUi, "#app")
  }
}
