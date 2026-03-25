package com.jeanfit.app.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

private val activityLevels = listOf(
    Triple("sedentary", "Sitzend", "Bürojob, kaum Bewegung"),
    Triple("lightly_active", "Leicht aktiv", "1-3x Sport pro Woche"),
    Triple("active", "Aktiv", "3-5x Sport pro Woche"),
    Triple("very_active", "Sehr aktiv", "6-7x intensiver Sport"),
)

@Composable
fun ActivityLevelScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selected by remember { mutableStateOf(state.activityLevel) }

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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OnboardingProgress(currentStep = 4, totalSteps = 8)
            Spacer(Modifier.height(24.dp))
            Text(
                "Wie aktiv bist du?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Denk an deine typische Woche.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                activityLevels.forEach { (value, title, desc) ->
                    val isSelected = selected == value
                    OutlinedCard(
                        onClick = { selected = value; viewModel.setActivityLevel(value) },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) SunsetOrange else MaterialTheme.colorScheme.outline
                        ),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSelected) SunsetOrange.copy(alpha = 0.08f)
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) SunsetOrange else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
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
private fun ActivityLevelPreview() {
    JeanFitTheme { ActivityLevelScreen(onNext = {}, onBack = {}) }
}
