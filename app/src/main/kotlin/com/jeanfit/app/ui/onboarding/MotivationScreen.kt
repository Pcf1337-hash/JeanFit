package com.jeanfit.app.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeanfit.app.ui.theme.JeanFitTheme
import com.jeanfit.app.ui.theme.SunsetOrange

private val motivations = listOf(
    "Mehr Energie im Alltag",
    "Besser aussehen und fühlen",
    "Krankheiten vorbeugen",
    "Für meine Familie gesund bleiben",
    "Sportliche Ziele erreichen",
    "Selbstvertrauen aufbauen",
    "Schlaf verbessern",
    "Stress reduzieren"
)

@Composable
fun MotivationScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selected by remember { mutableStateOf(state.motivation) }
    var bigPicture by remember { mutableStateOf(state.bigPicture) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Zurück") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OnboardingProgress(currentStep = 6, totalSteps = 8)
            Spacer(Modifier.height(24.dp))
            Text("Was motiviert dich?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Dein 'Warum' ist dein stärkster Antrieb.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                motivations.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { option ->
                            val isSelected = selected == option
                            OutlinedButton(
                                onClick = { selected = option; viewModel.setMotivation(option) },
                                modifier = Modifier.weight(1f),
                                border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) SunsetOrange else MaterialTheme.colorScheme.outline),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) SunsetOrange.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text(option, style = MaterialTheme.typography.bodySmall, color = if (isSelected) SunsetOrange else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Dein großes Bild (optional)", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = bigPicture,
                onValueChange = { bigPicture = it; viewModel.setBigPicture(it) },
                label = { Text("Beschreibe dein Ziel in einem Satz...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = MaterialTheme.shapes.large
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange)
            ) {
                Text("Weiter", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MotivationPreview() {
    JeanFitTheme { MotivationScreen(onNext = {}, onBack = {}) }
}
