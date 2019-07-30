package com.github.m50d.plusminuszero

import cats.syntax.apply._
import cats.syntax.functor._
import us.oyanglul.owlet._
import DOM._

case class TournamentResult(weight: Double, points: Int, name: Option[String] = None)

object TournamentResult {
  def weight: Owlet[Double] = label(number("weight", 0), "Weight")
  def points: Owlet[Int] = label(number("points", 0), "Points").map(_.toInt)
  def tournamentResult: Owlet[TournamentResult] =
    (weight, points).mapN(apply(_, _, None))

  val Zero = TournamentResult(1, 0, None)
}