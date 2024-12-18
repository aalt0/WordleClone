package com.example.wordleclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.wordleclone.ui.main.MainScreen
import com.example.wordleclone.ui.main.MainViewModel
import com.example.wordleclone.ui.theme.WordleCloneTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel by viewModel<MainViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val uiState = viewModel.uiState.collectAsStateWithLifecycle()
            WordleCloneTheme { MainScreen(uiState.value) }
        }
    }
}
