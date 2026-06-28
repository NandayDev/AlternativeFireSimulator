package dev.nanday.alternativefirecalculator.models

import kotlin.math.pow
import kotlin.math.roundToInt

data class FireSimulationParameters(
    /**
     * Current year today
     */
    val currentYear: Int,
    /**
     * How much is invested at the start of the simulation
     */
    val initialCapital: Int,

    /**
     * Net income of the initial year of the simulation
     */
    val yearlyIncomeInTodayEuros: Int,

    /**
     * Last year in which the income will be generated
     */
    val lastIncomeYear: Int,

    /**
     * Expected return on investments. If 3%, should be 1.03. If -4%, should be 0.96
     */
    val expectedReturnOnInvestmentMultiplier: Double,

    /**
     * Expected volatility on prices
     */
    val expectedVolatility: Double,

    /**
     * Expected inflation. If 3%, should be 1.03. If -4%, should be 0.96
     */
    val expectedInflationMultiplier: Double,

    /**
     * Whether the starting yearly income should be adjusted to inflation each year
     */
    val shouldAdjustIncomeToInflation: Boolean,

    /**
     * Ordinary yearly expenses, in today's euro
     */
    val yearlyExpensesInTodayEuros: Int,

    /**
     * Expected extra expenses occurring during the lifetime, by year, in today's euros
     */
    private val extraExpenses: Map<Int, List<FireSimulationExtraExpense>>,

    /**
     * Expected extra income events occuring during the lifetime, by year, in today's euros
     */
    private val extraIncomes: Map<Int, List<FireSimulationExtraIncome>>,

    /**
     * Year in which the state's pension will start
     */
    val pensionYear: Int,

    /**
     * Amount of expected monthly pension in today's euro
     */
    val pensionMonthlyIncomeInTodayEuros: Int,

    /**
     * Multiplier by which the pension reflects the inflation. As of now (2025), should be 80% of inflation, so this should be 0,8
     */
    val percentageOfPensionInflationIncrease: Double,

    /**
     * Expected year of my earthly demise
     */
    val expectedYearOfDeath: Int,

    /**
     * The first year in which the full expenses kick in
     *
     * Useful for Coast FIRE simulations; for Full FIRE, it's the same as `currentYear`
     */
    val firstYearOfFullExpenses: Int,

    /**
     * Number of Monte Carlo simulations to run
     */
    val repetitions: Int
) {

    val totalIncomeByYear: Map<Int, Int>

    val totalExpensesByYear: Map<Int, Int>

    init {
        val totalIncomeByYear = mutableMapOf<Int, Int>()
        val totalExpensesByYear = mutableMapOf<Int, Int>()
        var currentPension = pensionMonthlyIncomeInTodayEuros * 13
        var currentIncome = yearlyIncomeInTodayEuros
        var currentExpenses = yearlyExpensesInTodayEuros
        for (year in currentYear..expectedYearOfDeath) {
            var totalIncome = 0
            var totalExpenses = 0
            val inflationMultiplier = expectedInflationMultiplier.pow(year - currentYear)
            val specificYearIncome = ((extraIncomes[year]?.sumOf { it.amount } ?: 0) * inflationMultiplier).roundToInt()
            totalIncome += specificYearIncome
            if (year >= pensionYear) {
                totalIncome += currentPension
            }
            val specificYearExpenses = ((extraExpenses[year]?.sumOf { it.amount } ?: 0) * (inflationMultiplier)).roundToInt()
            totalExpenses += specificYearExpenses
            if (year <= lastIncomeYear) {
                totalIncome += currentIncome
            }
            totalExpenses += if (year >= firstYearOfFullExpenses) currentExpenses else 0
            totalIncomeByYear[year] = totalIncome
            totalExpensesByYear[year] = totalExpenses
            currentPension += (currentPension * (expectedInflationMultiplier - 1.0) * percentageOfPensionInflationIncrease).roundToInt()
            currentExpenses = (currentExpenses * expectedInflationMultiplier).roundToInt()
            currentIncome = if (lastIncomeYear < year) {
                0
            } else {
                if (shouldAdjustIncomeToInflation) {
                    (currentIncome * expectedInflationMultiplier).roundToInt()
                } else {
                    currentIncome
                }
            }
        }
        this.totalIncomeByYear = totalIncomeByYear
        this.totalExpensesByYear = totalExpensesByYear
    }
}

data class FireSimulationExtraExpense(
    val year: Int,
    val amount: Int,
    val description: String
)

data class FireSimulationExtraIncome(
    val year: Int,
    val amount: Int,
    val description: String
)