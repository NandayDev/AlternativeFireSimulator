package dev.nanday.alternativefirecalculator.ui

import dev.nanday.alternativefirecalculator.services.TimeProvider
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FireSimulationViewModelTest {

    private val mockTimeProvider = object : TimeProvider {
        override fun getCurrentDate(): LocalDate = LocalDate(2025, 1, 1)
        override fun getCurrentDateTimeString(): String = "2025-01-01T00:00:00Z"
        override fun daysInYear(year: Int): Int = 365
    }

    @Test
    fun `when initial capital is not a number, error should be true`() {
        val viewModel = FireSimulationFormViewModel(mockTimeProvider)
        
        viewModel.onInitialCapitalChange("abc")
        
        assertTrue(viewModel.uiState.value.initialCapitalError)
        assertFalse(viewModel.uiState.value.isFormValid)
    }

    @Test
    fun `when all fields are valid, isFormValid should be true`() {
        val viewModel = FireSimulationFormViewModel(mockTimeProvider)
        
        // Initial state is valid by default in my implementation, 
        // but let's re-verify after some changes
        viewModel.onInitialCapitalChange("50000")
        viewModel.onYearlyIncomeChange("30000")
        
        assertFalse(viewModel.uiState.value.initialCapitalError)
        assertTrue(viewModel.uiState.value.isFormValid)
    }
}
