package com.example.wordleclone.domain.repo

interface WordleCloneRepo {
    suspend fun getWord(): Result<String>
}