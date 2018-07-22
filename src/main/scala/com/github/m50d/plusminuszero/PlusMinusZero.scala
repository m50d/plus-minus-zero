package com.github.m50d.plusminuszero

import cats.instances.double._
import cats.instances.list._
import cats.instances.vector._
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.foldable._
import cats.syntax.functor._
import cats.syntax.traverse._
import scala.scalajs.js.annotation._
import us.oyanglul.owlet._
import org.scalajs.dom._
import DOM._
import monix.reactive.subjects.Var
import monix.execution.Scheduler.Implicits.global
import Function.const
import cats.Foldable

case class TournamentResult(weight: Double, points: Int)

object TournamentResult {
  def weight: Owlet[Double] = label(number("weight", 0), "Weight")
  def points: Owlet[Int] = label(number("points", 0), "Points").map(_.toInt)
  def tournamentResult: Owlet[TournamentResult] =
    (weight, points).mapN(apply)

  val Zero = TournamentResult(1, 0)
}

object Rank {
  private def weightedAverage[F[_]: Foldable](results: F[TournamentResult]) =
    (results.foldMap(r => r.points * r.weight)) / results.foldMap(_.weight)

  def partA(results: Vector[TournamentResult]) = {
    val (best5, rest) = results.padTo(5, TournamentResult.Zero).splitAt(5)
    val toDrop = rest.size / 5
    weightedAverage(best5 ++ rest.dropRight(toDrop))
  }

  def partB(results: Vector[TournamentResult]) =
    weightedAverage(results.padTo(4, TournamentResult.Zero).take(4))

  def ranking(results: Vector[TournamentResult]) = {
    val sortedResults = results.sortBy(_.points).reverse
    0.5 * partA(sortedResults) + 0.5 * partB(sortedResults)
  }
}

@JSExportTopLevel("plusMinusZero.PlusMinusZero")
object PlusMinusZero {
  @JSExport
  def main(args: Array[String]): Unit = {
    val toAdd = Var(None): Var[Option[TournamentResult]]
    val added = toAdd.scan(Vector[TournamentResult]())(_ ++ _)
    val toRemove = Var(None): Var[Option[TournamentResult]]
    val removed = toRemove.scan(Vector[TournamentResult]())(_ ++ _)
    val listOfResults = added.combineLatestMap(removed)(_ diff _)

    val explanation = {
      val el: html.Div = document.createElement("div").asInstanceOf[html.Div]
      val text = document.createTextNode("""
In "weight", enter MERS weight for tournaments in the past year and MERS weight/2
for tournaments between 1 and 2 years ago.

In "points", enter EMA points for your place in the tournament - 1000 for first
and around 0 for last.
""")
      el.appendChild(text)
      Owlet(List(el), Var(()))
    }
    
    val resultEntry = div(TournamentResult.tournamentResult, Var(Seq.empty))
    val addNewResult = (resultEntry, button("add", false, true)).mapN((
      (entry, pressed) => toAdd := (if (pressed) Some(entry) else None)))

    val rankUi = {
      val sink = Var(())
      val el: html.Div = document.createElement("div").asInstanceOf[html.Div]
      listOfResults foreach { results =>
        while (el.lastChild != null) {
          el.removeChild(el.lastChild)
        }
        results foreach { result =>
          val rel = document.createElement("div").asInstanceOf[html.Div]
          val tn = document.createTextNode(result.toString)
          rel.appendChild(tn)
          val removeButton =
          document.createElement("button").asInstanceOf[html.Button]
          removeButton.appendChild(document.createTextNode("x"))
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
      Owlet(List(el), sink)
    }
    render(explanation *> addNewResult *> rankUi, "#app")
  }
}