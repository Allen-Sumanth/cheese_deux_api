package com.example.cheese_deux_api

data class GameState(
    val currentTrack: Int = 1,
    val firstHit: Boolean = false,
    val firstHitScore: Int = 0,
    val gameStatus: GameStatus = GameStatus.STOPPED,
    var soundOn: Boolean = true
)

data class HackerState(
    val isHackerState: Boolean = true,
    val speedUp: Boolean = false,
    val speedUpActivationScore: Int = 0,
    val invulnerability: Boolean = false,
    val invulnerabilityActivationScore: Int = 0
)

data class HackerPlusState(
    val isHackerPlusState: Boolean = true,
    val cheeseCount: Int = 0,
    val latestCheeseScore: Int = 0//ensures cheese doesn't get added more than once
)

enum class GameStatus {
    PLAYING,
    PAUSED,
    STOPPED,
}