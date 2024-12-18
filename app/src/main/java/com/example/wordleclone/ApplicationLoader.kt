package com.example.wordleclone

import android.app.Application
import com.example.wordleclone.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin

class WordleCloneApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@WordleCloneApp)
            modules(appModule)
        }
    }
}