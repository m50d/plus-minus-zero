package com.github.m50d.plusminuszero

import org.junit.Test
import org.junit.Assert.assertEquals 

class RankTest {
  @Test def aidan(): Unit =
    assertEquals(468.05, Rank.ranking(Vector(
        TournamentResult(1, 387),
        TournamentResult(3, 835),
        TournamentResult(1.25, 200)
        )), 0.01)
        
   @Test def christopher(): Unit =
    assertEquals(526.62, Rank.ranking(Vector(
        TournamentResult(1, 484),
        TournamentResult(2.5, 522),
        TournamentResult(1.25, 548),
        TournamentResult(1.25, 743)
        )), 0.01)
        
  @Test def tony(): Unit =
    assertEquals(552.47, Rank.ranking(Vector(
        TournamentResult(1, 194),
        TournamentResult(2.5, 769),
        TournamentResult(1, 829),
        TournamentResult(3, 197),
        TournamentResult(1.25, 886)
    )), 0.01)
        
  @Test def daina(): Unit =
    assertEquals(789.41, Rank.ranking(Vector(
        TournamentResult(2.5, 359),
        TournamentResult(1, 974),
        TournamentResult(1.25, 739),
        TournamentResult(1.25, 652),
        TournamentResult(3, 945),
        TournamentResult(1.5, 184),
        TournamentResult(1.25, 933)
    )), 0.01)
}