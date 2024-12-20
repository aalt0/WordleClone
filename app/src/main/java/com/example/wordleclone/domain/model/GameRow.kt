package com.example.wordleclone.domain.model

data class GameRow(
    val rowNumber: Int,
    val state: RowState = RowState.INACTIVE,
    val entries: List<Character> = List(5) { Character() }
)
