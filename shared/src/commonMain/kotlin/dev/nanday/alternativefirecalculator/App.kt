package dev.nanday.alternativefirecalculator

import alternativefirecalculator.shared.generated.resources.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.nanday.alternativefirecalculator.models.FireSimulationParameters
import dev.nanday.alternativefirecalculator.ui.FireSimulationFormScreen
import dev.nanday.alternativefirecalculator.ui.FireSimulationResultScreen
import dev.nanday.alternativefirecalculator.ui.theme.AppTheme
import org.jetbrains.compose.resources.LocalResourceReader
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    val systemIsDark = isSystemInDarkTheme()
    var isDarkTheme by remember { mutableStateOf(systemIsDark) }
    
    val platform = remember { getPlatform() }
    var currentLanguage by remember { mutableStateOf(if (platform.language.startsWith("it")) "it" else "en") }

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
                                stringResource(Res.string.app_title),
                                style = MaterialTheme.typography.headlineMedium
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Icon(
                                painter = painterResource(Res.drawable.fire),
                                contentDescription = null
                            )

                            Spacer(
                                modifier = Modifier.weight(1F)
                            )

                            Text(
                                stringResource(Res.string.author_tag),
                                style = MaterialTheme.typography.bodySmall
                            )

                            IconButton(
                                onClick = { isDarkTheme = !isDarkTheme },
                                modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                            ) {
                                Icon(
                                    painter = painterResource(if (isDarkTheme) Res.drawable.light_mode else Res.drawable.dark_mode),
                                    contentDescription = if (isDarkTheme) stringResource(Res.string.set_light_mode) else stringResource(Res.string.set_dark_mode)
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
                                    contentDescription = stringResource(Res.string.back)
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
                    stringResource(Res.string.disclaimer),
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}

@Composable
fun LanguageFlag(
    painter: androidx.compose.ui.graphics.painter.Painter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .pointerHoverIcon(PointerIcon.Hand),
        contentScale = ContentScale.Crop
    )
}
