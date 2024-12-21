package com.example.wordleclone.di

import com.example.wordleclone.data.repo.WordListRepoImpl
import com.example.wordleclone.domain.repo.WordListRepo
import com.example.wordleclone.ui.main.MainViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import retrofit2.Retrofit

private const val BASE_URL = "https://raw.githubusercontent.com/seanpatlan/wordle-words/refs/heads/main/word-bank.csv"

val appModule = module {

    single<Retrofit> {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .build()
    }

//    single<WordListService> {
//        get<Retrofit>().create(WordListService::class.java)
//    }

    singleOf(::WordListRepoImpl) { bind<WordListRepo>() }
    viewModelOf(::MainViewModel)
}
