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
    val a2 = Var(identity): Var[List[Owlet[String]] => List[Owlet[String]]]
    val actions = Var(identity): Var[List[Owlet[TournamentResult]] => List[Owlet[TournamentResult]]]
    val listOfTodos =
      actions.scan(List[Owlet[TournamentResult]]())((owlets, f) => f(owlets))

    val notAddItem = const(Nil) _
    val addItem = (tr: TournamentResult) => List(string("todo-item", tr.toString).map(_ => tr))

    val newTodo = div(TournamentResult.tournamentResult, Var(Seq("header")))
    val addNewTodo =
      (button("add", notAddItem, addItem) <*> newTodo)
        .map(t => actions := (a => (a) ::: t))

    val todoUl: Owlet[List[TournamentResult]] = removableList(listOfTodos, a2)

    val rankUi = {
      val sink = Var(())
      val el: html.Div = document.createElement("div").asInstanceOf[html.Div]
      listOfTodos.flatMap(_.sequence.signal) foreach { results =>
        while (el.lastChild != null) {
          el.removeChild(el.lastChild)
        }
        val score = Rank.ranking(results.toVector)
        val scoreTn = document.createTextNode(s"Score: $score")
        el.appendChild(scoreTn)
        sink := ()
      }
      Owlet(List(el), sink)
    }
    //    todoUl map {results => Rank.ranking(results.toVector)}

    render(addNewTodo *> todoUl *> rankUi, "#app")
  }
}