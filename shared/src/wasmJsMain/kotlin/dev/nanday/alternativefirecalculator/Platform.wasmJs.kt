package dev.nanday.alternativefirecalculator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.intl.Locale
import kotlinx.browser.window

class WasmPlatform: Platform {
    override val name: String = "Web with Kotlin/Wasm"
    override val language: String = window.localStorage.getItem("lang") ?: window.navigator.language
}

actual fun getPlatform(): Platform = WasmPlatform()

external object window {
    var __customLocale: String?
}