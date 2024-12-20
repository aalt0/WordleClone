package com.example.wordleclone.domain.model

sealed class ValidationError {
    data object WordWrongLength : ValidationError()
    data object WordNotInList : ValidationError()
    data class MissingPositionChar(val char: Char, val position: Int) : ValidationError()
    data class MissingRequiredChar(val char: Char, val requiredCount: Int) : ValidationError()
}
