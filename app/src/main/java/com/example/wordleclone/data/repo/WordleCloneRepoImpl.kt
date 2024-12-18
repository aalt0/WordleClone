package com.example.wordleclone.data.repo

import com.example.wordleclone.domain.repo.WordleCloneRepo

class WordleCloneRepoImpl : WordleCloneRepo {
    override suspend fun getWord(): Result<String> {
        return try {
            Result.success("trace")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}