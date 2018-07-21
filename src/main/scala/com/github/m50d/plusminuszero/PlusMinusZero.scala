package com.github.m50d.plusminuszero

import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import scala.scalajs.js.annotation._
import us.oyanglul.owlet._
import DOM._

@JSExportTopLevel("plusMinusZero.PlusMinusZero")
object PlusMinusZero {
  @JSExport
  def main(args: Array[String]): Unit = {
//    for {
//      nWeightedScores <- number("nWeightedScores", 0)
//      scoreEntries <- (1 to nWeightedScores).traverse {
//        i => number(s"weightedScore$i", 0)
//      }
//    } yield {}
   
    val a1 = number("a1", 1)
    val a2 = number("a2", 2)
    val a3 = number("a3", 3)
    val sum = fx((a: List[Double]) => a.sum, List(a1, a2, a3))
    val product = fx(((a: List[Double]) => a.product), List(a1, a2, a3))
    render(a1 *> a2 *> a3 *> sum *> product, "#app")
  }
}