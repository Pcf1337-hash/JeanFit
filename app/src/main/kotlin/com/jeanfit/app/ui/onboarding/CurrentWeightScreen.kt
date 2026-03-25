package com.jeanfit.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeanfit.app.ui.theme.JeanFitTheme
import com.jeanfit.app.ui.theme.SunsetOrange
import kotlin.math.roundToInt

@Composable
fun CurrentWeightScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var sliderValue by remember { mutableFloatStateOf(state.currentWeightKg) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OnboardingProgress(currentStep = 2, totalSteps = 8)
            Spacer(Modifier.height(32.dp))
            Text(
                text = "Wie viel wiegst du aktuell?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Ehrlichkeit hilft uns, den besten Plan für dich zu erstellen.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(48.dp))
            Text(
                text = "${sliderValue.roundToInt()} kg",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = SunsetOrange
            )
            Spacer(Modifier.height(24.dp))
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it; viewModel.setCurrentWeight(it) },
                valueRange = 40f..200f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(thumbColor = SunsetOrange, activeTrackColor = SunsetOrange)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("40 kg", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("200 kg", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange)
            ) {
                Text("Weiter", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CurrentWeightPreview() {
    JeanFitTheme { CurrentWeightScreen(onNext = {}, onBack = {}) }
}
