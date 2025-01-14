package com.example.wordleclone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wordleclone.ui.main.MainScreen
import com.example.wordleclone.ui.main.MainViewModel
import com.example.wordleclone.ui.theme.WordleCloneTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {
    private val viewModel by viewModel<MainViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            WordleCloneTheme {
                Box(Modifier.safeDrawingPadding()) {
                    MainScreen(
                        uiState = uiState,
                        onKeyPressed = { viewModel.onKeyPress(it) },
                        onResetClicked = { viewModel.onResetClicked() },
                        onAnimationEnded = { viewModel.onAnimationEnded() }
                    )
                }
            }
        }
    }
}
