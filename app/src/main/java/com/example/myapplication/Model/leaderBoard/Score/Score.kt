package com.example.myapplication.Model.leaderBoard.Score

import android.os.Build
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Represents a player's game score record.
 *
 * Migrated from Java to Kotlin `data class` to demonstrate:
 * - Automatic `equals()`, `hashCode()`, `toString()`, `copy()` generation
 * - Null safety with non-nullable types
 * - Compact syntax (52 lines Java → ~25 lines Kotlin)
 *
 * Maintains full Java interoperability — all existing Java callers
 * (Leaderboard.java, Player.java) work without modification.
 */
data class Score(
    val score: Int,
    val difficulty: Int,
    val playerName: String,
    @get:JvmName("getIsNew")
    @set:JvmName("setIsNew")
    var isNew: Boolean
) {
    val date: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    } else {
        ""
    }
}
