package dev.nanday.alternativefirecalculator.ui

import androidx.lifecycle.ViewModel
import dev.nanday.alternativefirecalculator.models.FireSimulationParameters
import dev.nanday.alternativefirecalculator.services.TimeProvider
import dev.nanday.alternativefirecalculator.services.TimeProviderImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FireSimulationFormState(
    val initialCapital: String = "100000",
    val yearlyIncomeInTodayEuros: String = "30000",
    val expectedReturnPercent: Int = 5,
    val expectedVolatilityPercent: Int = 15,
    val expectedInflationPercent: Int = 2,
    val shouldAdjustIncomeToInflation: Boolean = true,
    val yearlyExpensesInTodayEuros: String = "20000",
    val pensionYear: String = "2060",
    val pensionMonthlyIncomeInTodayEuros: String = "1500",
    val expectedYearOfDeath: String = "2090",

    // Validation flags
    val initialCapitalError: Boolean = false,
    val yearlyIncomeError: Boolean = false,
    val lastIncomeYearError: Boolean = false,
    val yearlyExpensesError: Boolean = false,
    val pensionYearError: Boolean = false,
    val pensionMonthlyIncomeError: Boolean = false,
    val expectedYearOfDeathError: Boolean = false
) {
    val isFormValid: Boolean
        get() = !initialCapitalError && !yearlyIncomeError && !lastIncomeYearError &&
                !yearlyExpensesError && !pensionYearError && !pensionMonthlyIncomeError &&
                !expectedYearOfDeathError
}

class FireSimulationFormViewModel(
    private val timeProvider: TimeProvider = TimeProviderImpl()
) : ViewModel() {
    private val _uiState = MutableStateFlow(FireSimulationFormState())
    val uiState = _uiState.asStateFlow()

    fun onInitialCapitalChange(value: String) {
        _uiState.update { it.copy(initialCapital = value, initialCapitalError = value.toIntOrNull() == null) }
    }

    fun onYearlyIncomeChange(value: String) {
        _uiState.update { it.copy(yearlyIncomeInTodayEuros = value, yearlyIncomeError = value.toIntOrNull() == null) }
    }

    fun onExpectedReturnChange(value: Int) {
        _uiState.update { it.copy(expectedReturnPercent = value) }
    }

    fun onVolatilityChange(value: Int) {
        _uiState.update { it.copy(expectedVolatilityPercent = value) }
    }

    fun onInflationChange(value: Int) {
        _uiState.update { it.copy(expectedInflationPercent = value) }
    }

    fun onAdjustIncomeToInflationChange(value: Boolean) {
        _uiState.update { it.copy(shouldAdjustIncomeToInflation = value) }
    }

    fun onYearlyExpensesChange(value: String) {
        _uiState.update { it.copy(yearlyExpensesInTodayEuros = value, yearlyExpensesError = value.toIntOrNull() == null) }
    }

    fun onPensionYearChange(value: String) {
        val currentYear = timeProvider.getCurrentDate().year
        val year = value.toIntOrNull()
        val error = year == null || year < currentYear
        _uiState.update { it.copy(pensionYear = value, pensionYearError = error) }
    }

    fun onPensionIncomeChange(value: String) {
        _uiState.update { it.copy(pensionMonthlyIncomeInTodayEuros = value, pensionMonthlyIncomeError = value.toIntOrNull() == null) }
    }

    fun onYearOfDeathChange(value: String) {
        val currentYear = timeProvider.getCurrentDate().year
        val year = value.toIntOrNull()
        val error = year == null || year <= currentYear
        _uiState.update { it.copy(expectedYearOfDeath = value, expectedYearOfDeathError = error) }
    }

    fun submit(): FireSimulationParameters? {
        val s = _uiState.value
        if (!s.isFormValid) return null

        return try {
            val currentYear = timeProvider.getCurrentDate().year
            FireSimulationParameters(
                currentYear = currentYear,
                initialCapital = s.initialCapital.toInt(),
                yearlyIncomeInTodayEuros = s.yearlyIncomeInTodayEuros.toInt(),
                lastIncomeYear = 0,
                expectedReturnOnInvestmentMultiplier = 1.0 + (s.expectedReturnPercent / 100.0),
                expectedVolatility = s.expectedVolatilityPercent / 100.0,
                expectedInflationMultiplier = 1.0 + (s.expectedInflationPercent / 100.0),
                shouldAdjustIncomeToInflation = s.shouldAdjustIncomeToInflation,
                yearlyExpensesInTodayEuros = s.yearlyExpensesInTodayEuros.toInt(),
                extraExpenses = emptyMap(),
                extraIncomes = emptyMap(),
                pensionYear = s.pensionYear.toInt(),
                pensionMonthlyIncomeInTodayEuros = s.pensionMonthlyIncomeInTodayEuros.toInt(),
                percentageOfPensionInflationIncrease = 0.8,
                expectedYearOfDeath = s.expectedYearOfDeath.toInt(),
                firstYearOfFullExpenses = currentYear
            )
        } catch (e: Exception) {
            null
        }
    }
}
