package dev.nanday.alternativefirecalculator.services

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.random.Random

interface RandomReturnsGenerator {
    fun generate(
        numYears: Int,
        mean: Double,
        stdDev: Double
    ): List<Double>
}

class RandomReturnsGeneratorImpl : RandomReturnsGenerator {
    val rng = Random.Default

    override fun generate(
        numYears: Int,
        mean: Double,
        stdDev: Double
    ): List<Double> {
        val returns = mutableListOf<Double>()

        repeat(numYears) {
            val z = boxMullerTransform(rng)
            val r = mean + stdDev * z
            returns.add(r)
        }

        return returns
    }

    // Box-Muller per generare una variabile casuale normale standard (media 0, dev std 1)
    private fun boxMullerTransform(rng: Random): Double {
        val u1 = rng.nextDouble()
        val u2 = rng.nextDouble()
        return sqrt(-2.0 * ln(u1)) * cos(2.0 * PI * u2)
    }
}