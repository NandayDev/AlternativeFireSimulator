package dev.nanday.alternativefirecalculator.models

data class FireSimulationOutcome(
    /**
     * Final amount of invested money
     */
    val finalInvestedAmount: Int,

    /**
     * Progressive amount of invested money, by year
     */
    val investedAmountByYear: Map<Int, Int>,

    /**
     * Whether the simulation went underwater, and thus failed
     */
    val wentUnderwater: Boolean
)