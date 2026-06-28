package dev.nanday.alternativefirecalculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import alternativefirecalculator.shared.generated.resources.*
import dev.nanday.alternativefirecalculator.models.FireSimulationParameters
import dev.nanday.alternativefirecalculator.utils.formatThousands
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FireSimulationResultScreen(
    modifier: Modifier = Modifier,
    parameters: FireSimulationParameters,
    viewModel: FireSimulationResultViewModel = viewModel { FireSimulationResultViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedModel by remember { mutableStateOf<FireSimulationPageUiModel?>(null) }

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
                            SimulationCard(model) {
                                selectedModel = model
                            }
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

        selectedModel?.let { model ->
            SimulationDetailDialog(
                model = model,
                onDismiss = { selectedModel = null }
            )
        }
    }
}

@Composable
fun SimulationDetailDialog(
    model: FireSimulationPageUiModel,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(8.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.fire_year, model.year),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(Res.string.success_rate, model.successPercentage),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(model.color)
                )

                Spacer(modifier = Modifier.height(16.dp))

                SimulationChart(
                    simulationPaths = model.simulationPaths,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun SimulationChart(
    simulationPaths: List<Map<Int, Int>>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(color = Color.DarkGray, fontSize = 9.sp)

    Canvas(modifier = modifier) {
        if (simulationPaths.isEmpty()) return@Canvas

        // White background
        drawRect(color = Color.White)

        val paddingLeft = 60.dp.toPx()
        val paddingBottom = 30.dp.toPx()
        val paddingTop = 10.dp.toPx()
        val paddingRight = 10.dp.toPx()

        val chartWidth = size.width - paddingLeft - paddingRight
        val chartHeight = size.height - paddingTop - paddingBottom

        val allYears = simulationPaths.flatMap { it.keys }.distinct().sorted()
        if (allYears.isEmpty()) return@Canvas

        val minYear = allYears.first()
        val maxYear = allYears.last()
        val yearRange = (maxYear - minYear).toFloat().coerceAtLeast(1f)

        val allValues = simulationPaths.flatMap { it.values }
        val minValue = (allValues.minOrNull()?.toFloat() ?: 0f).coerceAtMost(0f)
        val maxValue = (allValues.maxOrNull()?.toFloat() ?: 1000000f).coerceAtLeast(100000f)
        val valueRange = (maxValue - minValue).coerceAtLeast(1f)

        // Draw Y Axis Labels and Grid Lines
        val ySteps = 5
        for (i in 0..ySteps) {
            val valY = minValue + (valueRange * i / ySteps)
            val y = paddingTop + chartHeight - (valY - minValue) / valueRange * chartHeight
            
            // Draw grid line
            drawLine(
                color = if (valY == 0f) Color.Gray else Color.LightGray.copy(alpha = 0.5f),
                start = Offset(paddingLeft, y),
                end = Offset(paddingLeft + chartWidth, y),
                strokeWidth = if (valY == 0f) 1.dp.toPx() else 0.5.dp.toPx()
            )

            // Draw text label
            val labelText = when {
                valY == 0f -> "0 €"
                valY >= 1_000_000f || valY <= -1_000_000f -> {
                    val value = valY / 1_000_000f
                    val rounded = (kotlin.math.round(value * 100) / 100f)
                    if (rounded % 1 == 0f) "${rounded.toInt()}M €" else "${rounded}M €"
                }
                valY >= 1_000f || valY <= -1_000f -> {
                    val value = valY / 1_000f
                    val rounded = (kotlin.math.round(value * 10) / 10f)
                    if (rounded % 1 == 0f) "${rounded.toInt()}k €" else "${rounded}k €"
                }
                else -> "${valY.toInt()} €"
            }
            
            val textLayoutResult = textMeasurer.measure(text = labelText, style = labelStyle)
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(paddingLeft - textLayoutResult.size.width - 4.dp.toPx(), y - textLayoutResult.size.height / 2)
            )
        }

        // Draw X Axis Labels (Years) and Vertical Grid Lines
        val xSteps = 5
        for (i in 0..xSteps) {
            val year = minYear + (yearRange * i / xSteps).toInt()
            val x = paddingLeft + (year - minYear).toFloat() / yearRange * chartWidth
            
            // Draw vertical grid line
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(x, paddingTop),
                end = Offset(x, paddingTop + chartHeight),
                strokeWidth = 0.5.dp.toPx()
            )

            val textLayoutResult = textMeasurer.measure(text = year.toString(), style = labelStyle)
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(x - textLayoutResult.size.width / 2, paddingTop + chartHeight + 4.dp.toPx())
            )
        }

        // Draw paths
        simulationPaths.forEach { path ->
            val sortedEntries = path.entries.sortedBy { it.key }
            val points = sortedEntries.map { (year, value) ->
                val x = paddingLeft + (year - minYear).toFloat() / yearRange * chartWidth
                val y = paddingTop + chartHeight - (value - minValue) / valueRange * chartHeight
                Offset(x, y)
            }

            if (points.size > 1) {
                val hasGoneUnderwater = sortedEntries.any { it.value < 0 }
                val color = if (hasGoneUnderwater) Color.Red else Color(0xFF4CAF50)

                val strokePath = Path().apply {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }
                drawPath(
                    path = strokePath,
                    color = color,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun SimulationCard(model: FireSimulationPageUiModel, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(model.color)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.fire_year, model.year),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = stringResource(Res.string.success_rate, model.successPercentage),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.average_capital,),
                style = MaterialTheme.typography.labelSmall,
                color = Color.Black
            )

            Text(
                text = "${formatThousands(model.averageCapitalAtYear.toLong())} €",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}
