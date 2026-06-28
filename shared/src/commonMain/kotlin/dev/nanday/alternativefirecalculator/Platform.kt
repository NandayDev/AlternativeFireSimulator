package dev.nanday.alternativefirecalculator

interface Platform {
    val name: String
    val language: String
}

expect fun getPlatform(): Platform