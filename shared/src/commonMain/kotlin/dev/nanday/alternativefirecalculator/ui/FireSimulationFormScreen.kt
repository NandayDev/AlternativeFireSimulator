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
import dev.nanday.alternativefirecalculator.models.FireSimulationParameters

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
            label = "Investiti oggi (€)",
            value = uiState.initialCapital,
            onValueChange = viewModel::onInitialCapitalChange,
            isError = uiState.initialCapitalError
        )
        
        FormField(
            label = "Reddito Annuale Netto (Oggi €)",
            value = uiState.yearlyIncomeInTodayEuros,
            onValueChange = viewModel::onYearlyIncomeChange,
            isError = uiState.yearlyIncomeError
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = uiState.shouldAdjustIncomeToInflation,
                onCheckedChange = viewModel::onAdjustIncomeToInflationChange
            )
            Text("Adegua reddito all'inflazione", style = MaterialTheme.typography.bodyMedium)
        }

        DropdownField(
            label = "Ritorno Atteso (%)",
            selected = uiState.expectedReturnPercent,
            options = (1..20).toList(),
            onOptionSelected = viewModel::onExpectedReturnChange
        )

        DropdownField(
            label = "Volatilità Attesa (%)",
            selected = uiState.expectedVolatilityPercent,
            options = (0..30).toList(),
            onOptionSelected = viewModel::onVolatilityChange
        )

        DropdownField(
            label = "Inflazione Attesa (%)",
            selected = uiState.expectedInflationPercent,
            options = (0..15).toList(),
            onOptionSelected = viewModel::onInflationChange
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        FormField(
            label = "Spese Annuali Ordinarie (Oggi €)",
            value = uiState.yearlyExpensesInTodayEuros,
            onValueChange = viewModel::onYearlyExpensesChange,
            isError = uiState.yearlyExpensesError
        )
        
        FormField(
            label = "Anno Inizio Pensione Statale",
            value = uiState.pensionYear,
            onValueChange = viewModel::onPensionYearChange,
            isError = uiState.pensionYearError
        )
        
        FormField(
            label = "Pensione Mensile Netta (Oggi €)",
            value = uiState.pensionMonthlyIncomeInTodayEuros,
            onValueChange = viewModel::onPensionIncomeChange,
            isError = uiState.pensionMonthlyIncomeError
        )
        
        FormField(
            label = "Anno Decesso Previsto",
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
            Text("Avvia Simulazione")
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
