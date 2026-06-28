package dev.nanday.alternativefirecalculator.services


import dev.nanday.alternativefirecalculator.models.FireSimulationOutcome
import dev.nanday.alternativefirecalculator.models.FireSimulationParameters
import kotlin.math.roundToInt

interface FireSimulator {
    fun simulate(
        parameters: FireSimulationParameters
    ): Result<FireSimulationOutcome>
}

class FireSimulatorImpl(
    private val randomReturnsGenerator: RandomReturnsGenerator,
    private val timeProvider: TimeProvider
) : FireSimulator {

    override fun simulate(parameters: FireSimulationParameters): Result<FireSimulationOutcome> {
        val currentYear = timeProvider.getCurrentDate().year
        val randomReturns = randomReturnsGenerator.generate(
            numYears = parameters.expectedYearOfDeath - currentYear + 1,
            mean = parameters.expectedReturnOnInvestmentMultiplier,
            stdDev = parameters.expectedVolatility
        )
        var index = 0
        var wentUnderwater = false
        var progressiveInvestedAmount = parameters.initialCapital
        val investedAmountByYear = mutableMapOf<Int, Int>()
        for (year in currentYear..parameters.expectedYearOfDeath) {
            progressiveInvestedAmount += parameters.totalIncomeByYear[year] ?: 0
            progressiveInvestedAmount -= parameters.totalExpensesByYear[year] ?: 0
            if (progressiveInvestedAmount > 0) {
                // Only apply returns when > 0, otherwise it doesn't make sense: money not invested if negative //
                progressiveInvestedAmount = (progressiveInvestedAmount * randomReturns[index]).roundToInt()
            }
            investedAmountByYear[year] = progressiveInvestedAmount
            wentUnderwater = wentUnderwater || progressiveInvestedAmount < 0
            index += 1
        }

        return Result.success(
            FireSimulationOutcome(
                finalInvestedAmount = progressiveInvestedAmount,
                investedAmountByYear = investedAmountByYear,
                wentUnderwater = wentUnderwater
            )
        )
    }
}