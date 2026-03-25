package com.jeanfit.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.jeanfit.app.ui.theme.SpringWood

@Composable
fun WelcomeScreen(
    onNext: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var name by remember { mutableStateOf(state.name) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "JeanFit",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = SunsetOrange
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Dein personalisierter Weg zu einem gesunden Lebensstil",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(48.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; viewModel.setName(it) },
            label = { Text("Dein Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onNext,
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange)
        ) {
            Text("Los geht's", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomeScreenPreviewLight() {
    JeanFitTheme { WelcomeScreen(onNext = {}) }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun WelcomeScreenPreviewDark() {
    JeanFitTheme(darkTheme = true) { WelcomeScreen(onNext = {}) }
}
