package dev.nanday.alternativefirecalculator.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nanday.alternativefirecalculator.models.FireSimulationParameters
import dev.nanday.alternativefirecalculator.services.FireSimulator
import dev.nanday.alternativefirecalculator.services.FireSimulatorImpl
import dev.nanday.alternativefirecalculator.services.RandomReturnsGeneratorImpl
import dev.nanday.alternativefirecalculator.services.TimeProviderImpl
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.roundToInt

data class FireSimulationPageUiModel(
    val year: String,
    val successPercentage: String,
    val color: Long,
    val minCapitalAtYear: Int,
    val maxCapitalAtYear: Int,
    val averageCapitalAtYear: Double,
    val parameters: FireSimulationParameters,
    val simulationPaths: List<Map<Int, Int>>
)

sealed class FireSimulationResultUiState {
    object Loading : FireSimulationResultUiState()
    data class Success(
        val originalParameters: FireSimulationParameters,
        val models: List<FireSimulationPageUiModel>
    ) : FireSimulationResultUiState()
    data class Error(val message: String) : FireSimulationResultUiState()
}

class FireSimulationResultViewModel(
    private val fireSimulator: FireSimulator = FireSimulatorImpl(RandomReturnsGeneratorImpl(), TimeProviderImpl())
) : ViewModel() {
    private val _uiState = MutableStateFlow<FireSimulationResultUiState>(FireSimulationResultUiState.Loading)
    val uiState = _uiState.asStateFlow()
    
    private var simulationJob: Job? = null

    fun startSimulation(startingParameters: FireSimulationParameters) {
        // If we are already showing results for these parameters, don't restart
        val currentState = _uiState.value
        if (currentState is FireSimulationResultUiState.Success && currentState.originalParameters == startingParameters) {
            return
        }

        simulationJob?.cancel()
        _uiState.value = FireSimulationResultUiState.Loading
        
        simulationJob = viewModelScope.launch(Dispatchers.Default) {
            val repetitions = startingParameters.repetitions
            val yearsTotal = 30
            val uiModels = mutableListOf<FireSimulationPageUiModel>()
            val mutex = Mutex()
            val deferredList = mutableListOf<Deferred<*>>()

            val startingYear = startingParameters.currentYear

            for (year in startingYear..startingYear + yearsTotal) {
                val deferred = async(Dispatchers.Default) {
                    var positiveSimulations = 0
                    val capitalAtYearList = mutableListOf<Int>()
                    var minCapitalAtYear = Int.MAX_VALUE
                    var maxCapitalAtYear = 0
                    val simulationPaths = mutableListOf<Map<Int, Int>>()

                    val parametersForThisYear = startingParameters.copy(
                        lastIncomeYear = year
                    )

                    repeat(repetitions) {
                        fireSimulator.simulate(parametersForThisYear)
                            .onSuccess { outcome ->
                                val capitalAtYear = outcome.investedAmountByYear[year]
                                minCapitalAtYear = minOf(minCapitalAtYear, capitalAtYear ?: Int.MAX_VALUE)
                                maxCapitalAtYear = maxOf(maxCapitalAtYear, capitalAtYear ?: 0)
                                if (!outcome.wentUnderwater) {
                                    positiveSimulations++
                                }
                                capitalAtYear?.let { capitalAtYearList.add(it) }
                                
                                // Keep a sample of 100 simulations for the chart
                                if (simulationPaths.size < 100) {
                                    simulationPaths.add(outcome.investedAmountByYear)
                                }
                            }.onFailure {
                                // Ignore single simulation failure
                            }
                    }

                    val positiveSimulationPercentage = (positiveSimulations.toDouble() / repetitions * 100.0).roundToInt()
                    val uiModel = FireSimulationPageUiModel(
                        year = year.toString(),
                        successPercentage = "$positiveSimulationPercentage%",
                        color = when {
                            positiveSimulationPercentage < 80 -> 0xFFF44336
                            positiveSimulationPercentage < 85 -> 0xFFFF9800
                            positiveSimulationPercentage < 90 -> 0xFFFFEB3B
                            positiveSimulationPercentage < 95 -> 0xFFCDDC39
                            else -> 0xFF4CAF50
                        },
                        minCapitalAtYear = minCapitalAtYear,
                        maxCapitalAtYear = maxCapitalAtYear,
                        averageCapitalAtYear = if (capitalAtYearList.isEmpty()) 0.0 else capitalAtYearList.average(),
                        parameters = parametersForThisYear,
                        simulationPaths = simulationPaths
                    )
                    mutex.withLock {
                        uiModels.add(uiModel)
                    }
                }
                deferredList.add(deferred)
            }

            deferredList.forEach { it.await() }

            val sortedUiModels = uiModels.sortedBy { it.year }
            _uiState.value = FireSimulationResultUiState.Success(startingParameters, sortedUiModels)
        }
    }
}
