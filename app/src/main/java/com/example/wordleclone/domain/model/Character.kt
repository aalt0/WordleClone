package com.example.wordleclone.domain.model

enum class CharState { NO_MATCH, MATCH_IN_WORD, MATCH_IN_POSITION }

data class Character(
    val char: String = "",
    val charState: CharState = CharState.NO_MATCH
)
