package dev.nanday.alternativefirecalculator

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.nanday.alternativefirecalculator.di.initKoin
import kotlinx.browser.document
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val lang = window.localStorage.getItem("lang") ?: window.navigator.language
    document.title = if (lang.startsWith("it")) {
        "Simulatore FIRE alternativo"
    } else {
        "Alternative FIRE Simulator"
    }
    
    // Set the lang attribute of the html tag
    document.documentElement?.setAttribute("lang", if (lang.startsWith("it")) "it" else "en")

    initKoin {  }
    ComposeViewport {
        App()
    }
}