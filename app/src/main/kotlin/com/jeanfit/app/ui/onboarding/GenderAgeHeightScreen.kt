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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeanfit.app.ui.theme.JeanFitTheme
import com.jeanfit.app.ui.theme.SunsetOrange
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun GenderAgeHeightScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedGender by remember { mutableStateOf(state.gender) }
    var birthYear by remember { mutableIntStateOf(state.birthDate.year) }
    var heightValue by remember { mutableFloatStateOf(state.heightCm) }

    val genders = listOf(
        "male" to "Mann",
        "female" to "Frau",
        "non_binary" to "Divers",
        "prefer_not" to "Keine Angabe"
    )

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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OnboardingProgress(currentStep = 3, totalSteps = 8)
            Spacer(Modifier.height(24.dp))
            Text("Erzähl uns von dir", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))

            // Gender
            Text("Geschlecht", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                genders.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (value, label) ->
                            val isSelected = selectedGender == value
                            OutlinedButton(
                                onClick = { selectedGender = value; viewModel.setGender(value) },
                                modifier = Modifier.weight(1f).height(48.dp),
                                border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) SunsetOrange else MaterialTheme.colorScheme.outline),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) SunsetOrange.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text(label, color = if (isSelected) SunsetOrange else MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            // Birth Year
            Text("Geburtsjahr", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text("$birthYear", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = SunsetOrange)
            Slider(
                value = birthYear.toFloat(),
                onValueChange = {
                    birthYear = it.roundToInt()
                    viewModel.setBirthDate(LocalDate.of(birthYear, 1, 1))
                },
                valueRange = 1940f..2010f,
                steps = 69,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(thumbColor = SunsetOrange, activeTrackColor = SunsetOrange)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("1940", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("2010", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(24.dp))
            // Height
            Text("Körpergröße", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text("${heightValue.roundToInt()} cm", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = SunsetOrange)
            Slider(
                value = heightValue,
                onValueChange = { heightValue = it; viewModel.setHeight(it) },
                valueRange = 140f..220f,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(thumbColor = SunsetOrange, activeTrackColor = SunsetOrange)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("140 cm", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("220 cm", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(32.dp))
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
private fun GenderAgeHeightPreview() {
    JeanFitTheme { GenderAgeHeightScreen(onNext = {}, onBack = {}) }
}
