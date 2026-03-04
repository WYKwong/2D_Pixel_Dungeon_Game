package com.example.myapplication.Model.leaderBoard.Score

import java.util.Comparator

/**
 * Kotlin comparators for Score sorting, demonstrating functional
 * programming style with `compareByDescending`.
 *
 * Replaces two separate Java files (ScoreComparator.java,
 * DifficultyComparator.java) with concise Kotlin lambdas.
 *
 * Usage from Java is unchanged:
 *   playerRecords.sort(new ScoreComparator());
 *   playerRecords.sort(new DifficultyComparator());
 */
class ScoreComparator : Comparator<Score> {
    override fun compare(p1: Score, p2: Score): Int =
        compareValuesBy(p2, p1) { it.score }
}

class DifficultyComparator : Comparator<Score> {
    override fun compare(p1: Score, p2: Score): Int =
        compareValuesBy(p2, p1) { it.difficulty }
}
