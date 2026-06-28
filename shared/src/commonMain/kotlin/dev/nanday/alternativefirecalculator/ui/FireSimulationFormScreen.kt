package dev.nanday.alternativefirecalculator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import alternativefirecalculator.shared.generated.resources.*
import dev.nanday.alternativefirecalculator.models.FireSimulationParameters
import org.jetbrains.compose.resources.stringResource

@Composable
fun FireSimulationFormScreen(
    modifier: Modifier = Modifier,
    onSimulationStarted: (FireSimulationParameters) -> Unit,
    viewModel: FireSimulationFormViewModel = viewModel { FireSimulationFormViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FormField(
            label = stringResource(Res.string.initial_capital),
            value = uiState.initialCapital,
            onValueChange = viewModel::onInitialCapitalChange,
            isError = uiState.initialCapitalError
        )
        
        FormField(
            label = stringResource(Res.string.yearly_income),
            value = uiState.yearlyIncomeInTodayEuros,
            onValueChange = viewModel::onYearlyIncomeChange,
            isError = uiState.yearlyIncomeError
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = uiState.shouldAdjustIncomeToInflation,
                onCheckedChange = viewModel::onAdjustIncomeToInflationChange
            )
            Text(stringResource(Res.string.adjust_income), style = MaterialTheme.typography.bodyMedium)
        }

        DropdownField(
            label = stringResource(Res.string.expected_return),
            selected = uiState.expectedReturnPercent,
            options = (1..20).toList(),
            onOptionSelected = viewModel::onExpectedReturnChange
        )

        DropdownField(
            label = stringResource(Res.string.expected_volatility),
            selected = uiState.expectedVolatilityPercent,
            options = (0..30).toList(),
            onOptionSelected = viewModel::onVolatilityChange
        )

        DropdownField(
            label = stringResource(Res.string.expected_inflation),
            selected = uiState.expectedInflationPercent,
            options = (0..15).toList(),
            onOptionSelected = viewModel::onInflationChange
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        FormField(
            label = stringResource(Res.string.yearly_expenses),
            value = uiState.yearlyExpensesInTodayEuros,
            onValueChange = viewModel::onYearlyExpensesChange,
            isError = uiState.yearlyExpensesError
        )
        
        FormField(
            label = stringResource(Res.string.pension_year),
            value = uiState.pensionYear,
            onValueChange = viewModel::onPensionYearChange,
            isError = uiState.pensionYearError
        )
        
        FormField(
            label = stringResource(Res.string.pension_income),
            value = uiState.pensionMonthlyIncomeInTodayEuros,
            onValueChange = viewModel::onPensionIncomeChange,
            isError = uiState.pensionMonthlyIncomeError
        )
        
        FormField(
            label = stringResource(Res.string.death_year),
            value = uiState.expectedYearOfDeath,
            onValueChange = viewModel::onYearOfDeathChange,
            isError = uiState.expectedYearOfDeathError
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { 
                viewModel.submit()?.let { params ->
                    onSimulationStarted(params)
                }
            },
            enabled = uiState.isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .pointerHoverIcon(PointerIcon.Hand)
        ) {
            Text(stringResource(Res.string.start_simulation))
        }
    }
}

@Composable
fun FormField(
    label: String, 
    value: String, 
    onValueChange: (String) -> Unit,
    isError: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    selected: Int,
    options: List<Int>,
    onOptionSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = "$selected%",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true).fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text("$option%") },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
