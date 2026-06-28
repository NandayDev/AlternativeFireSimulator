package dev.nanday.alternativefirecalculator

import alternativefirecalculator.shared.generated.resources.Res
import alternativefirecalculator.shared.generated.resources.arrow_back
import alternativefirecalculator.shared.generated.resources.dark_mode
import alternativefirecalculator.shared.generated.resources.fire
import alternativefirecalculator.shared.generated.resources.light_mode
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.nanday.alternativefirecalculator.models.FireSimulationParameters
import dev.nanday.alternativefirecalculator.ui.FireSimulationFormScreen
import dev.nanday.alternativefirecalculator.ui.FireSimulationResultScreen
import dev.nanday.alternativefirecalculator.ui.theme.AppTheme
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    val systemIsDark = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(systemIsDark) }

    AppTheme(useDarkTheme = isDarkTheme) {
        var currentParameters by remember { mutableStateOf<FireSimulationParameters?>(null) }
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Row(
                            modifier = Modifier.padding(all = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Simulazione FIRE",
                                style = MaterialTheme.typography.headlineMedium
                            )

                            Icon(
                                painter = painterResource(Res.drawable.fire),
                                contentDescription = null
                            )

                            Spacer(
                                modifier = Modifier.weight(1F)
                            )

                            Text(
                                "by Guido Cardinali (nanday.dev)",
                                style = MaterialTheme.typography.bodySmall
                            )

                            IconButton(
                                onClick = { isDarkTheme = !isDarkTheme },
                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                            ) {
                                Icon(
                                    painter = painterResource(if (isDarkTheme) Res.drawable.light_mode else Res.drawable.dark_mode),
                                    contentDescription = if (isDarkTheme) "Imposta light mode" else "Imposta dark mode"
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        if (currentParameters != null) {
                            IconButton(
                                onClick = {
                                    currentParameters = null
                                },
                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.arrow_back),
                                    contentDescription = "indietro"
                                )
                            }
                        }
                    }
                )
            }
        ) {
            Column(modifier = Modifier.padding(it)) {
                if (currentParameters == null) {
                    FireSimulationFormScreen(
                        modifier = Modifier.weight(1F),
                        onSimulationStarted = { params ->
                            currentParameters = params
                        }
                    )
                } else {
                    FireSimulationResultScreen(
                        modifier = Modifier.weight(1F),
                        parameters = currentParameters!!
                    )
                }

                Text(
                    "Disclaimer: Dati, stime, rendimenti, simulazioni e calcoli riportati potrebbero contenere errori, imprecisioni o semplificazioni e non sono garantiti né necessariamente aggiornati o veritieri. Prima di effettuare qualsiasi investimento valuta autonomamente i rischi e consulta un professionista abilitato.",
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}
