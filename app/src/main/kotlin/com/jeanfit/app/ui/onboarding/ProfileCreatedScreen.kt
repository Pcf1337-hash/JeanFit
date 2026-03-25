package com.jeanfit.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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

@Composable
fun ProfileCreatedScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = SunsetOrange,
            modifier = Modifier.size(100.dp)
        )
        Spacer(Modifier.height(32.dp))
        Text(
            text = "Dein Plan ist bereit!",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Hallo ${state.name}! Wir haben deinen persönlichen Ernährungsplan erstellt.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(32.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SunsetOrange.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryRow("Tägliches Ziel", "${state.calculatedCalories} kcal")
                SummaryRow("Zielgewicht", "${state.goalWeightKg.toInt()} kg")
                SummaryRow("Startgewicht", "${state.currentWeightKg.toInt()} kg")
                SummaryRow("Abnehmen", "${(state.currentWeightKg - state.goalWeightKg).toInt()} kg")
            }
        }
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange)
        ) {
            Text("JeanFit starten", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = SunsetOrange)
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileCreatedPreview() {
    JeanFitTheme { ProfileCreatedScreen(onFinish = {}) }
}
