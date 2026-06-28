package dev.nanday.alternativefirecalculator

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform