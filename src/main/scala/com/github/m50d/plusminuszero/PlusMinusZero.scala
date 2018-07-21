package com.github.m50d.plusminuszero

import cats.instances.vector._
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import scala.scalajs.js.annotation._
import us.oyanglul.owlet._
import DOM._
import monix.reactive.subjects.Var
import monix.execution.Scheduler.Implicits.global
import Function.const

case class TournamentResult(weight: Double, points: Int)

object TournamentResult {
  def weight: Owlet[Double] = label(number("weight", 0), "Weight")
  def points: Owlet[Int] = label(number("points", 0), "Points").map(_.toInt)
  def tournamentResult: Owlet[TournamentResult] =
    (weight, points).mapN(apply)
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
        .map(t => actions := (a => (a ) ::: t))
        

    val todoUl: Owlet[List[TournamentResult]] = removableList(listOfTodos, a2)
    render(addNewTodo *> todoUl, "#app")
  }
}