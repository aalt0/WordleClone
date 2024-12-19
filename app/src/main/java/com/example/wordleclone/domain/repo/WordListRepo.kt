package com.example.wordleclone.domain.repo

interface WordListRepo {
    suspend fun getWord(): Result<String>
    fun isWordInWordList(word: String): Boolean
}