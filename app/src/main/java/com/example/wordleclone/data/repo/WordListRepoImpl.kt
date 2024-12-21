package com.example.wordleclone.data.repo

import android.content.Context
import com.example.wordleclone.domain.repo.WordListRepo

class WordListRepoImpl(private val context: Context) : WordListRepo {
    // we should keep track of the used words

    private var wordlist: Collection<String> = emptySet()

    override suspend fun getWord(): Result<String> {
        return try {
            if (wordlist.isEmpty()) {
                wordlist = context.assets.open("words.txt")
                    .bufferedReader()
                    .use { it.readText() }
                    .split('\n')
            }
            Result.success(wordlist.random().uppercase())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isWordInWordList(word: String) =
        wordlist.any { it.equals(word, ignoreCase = true) }
}
