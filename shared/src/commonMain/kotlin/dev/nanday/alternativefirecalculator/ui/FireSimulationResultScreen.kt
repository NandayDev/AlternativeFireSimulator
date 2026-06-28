package dev.nanday.alternativefirecalculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.graphics.drawscope.Fill
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

                        item(span = { GridItemSpan(maxLineSpan) }) {
                            ParametersSummaryCard(state.originalParameters)
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SimulationChart(
    simulationPaths: List<Map<Int, Int>>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(color = Color.DarkGray, fontSize = 9.sp)
    val tooltipStyle = TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    
    var pointerOffset by remember { mutableStateOf<Offset?>(null) }

    Canvas(
        modifier = modifier
            .onPointerEvent(PointerEventType.Move) { pointerOffset = it.changes.first().position }
            .onPointerEvent(PointerEventType.Enter) { pointerOffset = it.changes.first().position }
            .onPointerEvent(PointerEventType.Exit) { pointerOffset = null }
    ) {
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

        // Helper to format Y values
        fun formatY(valY: Float): String = when {
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

        // Draw Y Axis Labels and Grid Lines
        val ySteps = 5
        for (i in 0..ySteps) {
            val valY = minValue + (valueRange * i / ySteps)
            val y = paddingTop + chartHeight - (valY - minValue) / valueRange * chartHeight
            
            drawLine(
                color = if (valY == 0f) Color.Gray else Color.LightGray.copy(alpha = 0.5f),
                start = Offset(paddingLeft, y),
                end = Offset(paddingLeft + chartWidth, y),
                strokeWidth = if (valY == 0f) 1.dp.toPx() else 0.5.dp.toPx()
            )

            val labelText = formatY(valY)
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
        clipRect(left = paddingLeft, top = paddingTop, right = paddingLeft + chartWidth, bottom = paddingTop + chartHeight) {
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
                        color = color.copy(alpha = 0.2f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }

        // Draw Crosshair and Tooltips
        pointerOffset?.let { pos ->
            if (pos.x >= paddingLeft && pos.x <= paddingLeft + chartWidth &&
                pos.y >= paddingTop && pos.y <= paddingTop + chartHeight
            ) {
                // Vertical line
                drawLine(
                    color = Color.DarkGray,
                    start = Offset(pos.x, paddingTop),
                    end = Offset(pos.x, paddingTop + chartHeight),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )

                // Horizontal line
                drawLine(
                    color = Color.DarkGray,
                    start = Offset(paddingLeft, pos.y),
                    end = Offset(paddingLeft + chartWidth, pos.y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )

                // Calculate values
                val hoveredYear = (minYear + (pos.x - paddingLeft) / chartWidth * yearRange).toInt()
                val hoveredValue = minValue + (1 - (pos.y - paddingTop) / chartHeight) * valueRange

                // Tooltip X (Year)
                val yearText = hoveredYear.toString()
                val yearLayout = textMeasurer.measure(yearText, tooltipStyle)
                val yearRectWidth = yearLayout.size.width + 8.dp.toPx()
                val yearRectHeight = yearLayout.size.height + 4.dp.toPx()
                
                drawRect(
                    color = Color.DarkGray,
                    topLeft = Offset(pos.x - yearRectWidth / 2, paddingTop + chartHeight + 2.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(yearRectWidth, yearRectHeight)
                )
                drawText(
                    textLayoutResult = yearLayout,
                    topLeft = Offset(pos.x - yearLayout.size.width / 2, paddingTop + chartHeight + 4.dp.toPx())
                )

                // Tooltip Y (Value)
                val valueText = formatY(hoveredValue)
                val valueLayout = textMeasurer.measure(valueText, tooltipStyle)
                val valueRectWidth = valueLayout.size.width + 8.dp.toPx()
                val valueRectHeight = valueLayout.size.height + 4.dp.toPx()

                drawRect(
                    color = Color.DarkGray,
                    topLeft = Offset(paddingLeft - valueRectWidth - 2.dp.toPx(), pos.y - valueRectHeight / 2),
                    size = androidx.compose.ui.geometry.Size(valueRectWidth, valueRectHeight)
                )
                drawText(
                    textLayoutResult = valueLayout,
                    topLeft = Offset(paddingLeft - valueLayout.size.width - 6.dp.toPx(), pos.y - valueLayout.size.height / 2)
                )
            }
        }
    }
}

@Composable
fun ParametersSummaryCard(params: FireSimulationParameters) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(Res.string.simulation_parameters),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ParameterItem(stringResource(Res.string.initial_capital), "${formatThousands(params.initialCapital.toLong())} €")
                ParameterItem(stringResource(Res.string.yearly_income), "${formatThousands(params.yearlyIncomeInTodayEuros.toLong())} €")
                ParameterItem(stringResource(Res.string.yearly_expenses), "${formatThousands(params.yearlyExpensesInTodayEuros.toLong())} €")
                ParameterItem(stringResource(Res.string.expected_return), "${((params.expectedReturnOnInvestmentMultiplier - 1.0) * 100).toInt()}%")
                ParameterItem(stringResource(Res.string.volatility), "${(params.expectedVolatility * 100).toInt()}%")
                ParameterItem(stringResource(Res.string.inflation), "${((params.expectedInflationMultiplier - 1.0) * 100).toInt()}%")
                ParameterItem(stringResource(Res.string.pension_from), params.pensionYear.toString())
                ParameterItem(stringResource(Res.string.pension_income), "${formatThousands(params.pensionMonthlyIncomeInTodayEuros.toLong())} €")
                ParameterItem(stringResource(Res.string.death_year), params.expectedYearOfDeath.toString())
                ParameterItem(stringResource(Res.string.num_simulations), formatThousands(params.repetitions.toLong()))
            }
        }
    }
}

@Composable
fun ParameterItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
