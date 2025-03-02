package com.motycka.edu.game.match.model

enum class MatchOutcome {
    WIN, LOSS, DRAW;

    fun toDbString(): String {
        return this.name
    }

    companion object {
        fun fromString(value: String): MatchOutcome {
            return valueOf(value)
        }
    }
}