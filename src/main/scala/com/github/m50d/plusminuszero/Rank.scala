package com.github.m50d.plusminuszero

import cats.Foldable
import cats.instances.double._
import cats.instances.vector._
import cats.syntax.foldable._

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
