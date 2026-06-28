package dev.nanday.alternativefirecalculator

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.nanday.alternativefirecalculator.di.initKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin {  }
    ComposeViewport {
        App()
    }
}