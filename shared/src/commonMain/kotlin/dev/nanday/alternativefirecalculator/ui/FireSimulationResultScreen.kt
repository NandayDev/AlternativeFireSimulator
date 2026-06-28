package dev.nanday.alternativefirecalculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.nanday.alternativefirecalculator.models.FireSimulationParameters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FireSimulationResultScreen(
    modifier: Modifier = Modifier,
    parameters: FireSimulationParameters,
    viewModel: FireSimulationResultViewModel = viewModel { FireSimulationResultViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(parameters) {
        viewModel.startSimulation(parameters)
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (val state = uiState) {
            is FireSimulationResultUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is FireSimulationResultUiState.Success -> {
                if (state.originalParameters == parameters) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.models) { model ->
                            SimulationCard(model)
                        }
                    }
                } else {
                    // Parameters don't match yet, the LaunchedEffect will trigger startSimulation soon
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }

            is FireSimulationResultUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun SimulationCard(model: FireSimulationPageUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(model.color)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Anno FIRE: ${model.year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "Successo: ${model.successPercentage}",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Capitale iniziale (media):",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Black.copy(alpha = 0.7f)
            )
            Text(
                text = "${model.averageCapitalAtYear.toLong()}€",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}
