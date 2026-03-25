package com.jeanfit.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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

private val healthOptions = listOf(
    "Kein Diabetes",
    "Typ-2-Diabetes",
    "Bluthochdruck",
    "Herzerkrankung",
    "Gelenk-/Rückenprobleme",
    "Cholesterin erhöht",
    "Schilddrüsenprobleme",
    "Keine Einschränkungen"
)

@Composable
fun HealthConditionsScreen(
    onNext: () -> Unit,
    onBack: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

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
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OnboardingProgress(currentStep = 5, totalSteps = 8)
            Spacer(Modifier.height(24.dp))
            Text("Gesundheitliche Hinweise", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Wähle alles aus, was auf dich zutrifft. Nur zur besseren Planung.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(healthOptions) { option ->
                    val isSelected = option in state.healthConditions
                    Card(
                        onClick = { viewModel.toggleHealthCondition(option) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) SunsetOrange.copy(alpha = 0.08f)
                            else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                option,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                                color = if (isSelected) SunsetOrange else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = SunsetOrange)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
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
private fun HealthConditionsPreview() {
    JeanFitTheme { HealthConditionsScreen(onNext = {}, onBack = {}) }
}
