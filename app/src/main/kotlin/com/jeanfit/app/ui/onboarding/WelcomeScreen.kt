package com.jeanfit.app.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeanfit.app.ui.theme.JeanFitTheme
import com.jeanfit.app.ui.theme.SunsetOrange
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val WaveColor = Color(0xFF42A5F5)

@Composable
fun WelcomeScreen(
    onNext: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var name by remember { mutableStateOf(state.name) }

    // ── Pulsing wave animation ─────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "p1"
    )
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "a1"
    )

    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, delayMillis = 667, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "p2"
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, delayMillis = 667, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "a2"
    )

    val pulse3 by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, delayMillis = 1333, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "p3"
    )
    val alpha3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, delayMillis = 1333, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "a3"
    )

    // ── Staggered tagline fade-in ──────────────────────────────────────────
    val titleAlpha   = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }
    val fieldAlpha   = remember { Animatable(0f) }
    val buttonAlpha  = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            titleAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
        }
        delay(200)
        launch {
            taglineAlpha.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
        }
        delay(200)
        launch {
            fieldAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        }
        delay(150)
        launch {
            buttonAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Pulsing waves behind logo text
        Box(contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(200.dp)) {
                drawCircle(
                    color = WaveColor.copy(alpha = alpha1),
                    radius = center.x * pulse1
                )
                drawCircle(
                    color = WaveColor.copy(alpha = alpha2),
                    radius = center.x * pulse2
                )
                drawCircle(
                    color = WaveColor.copy(alpha = alpha3),
                    radius = center.x * pulse3
                )
            }

            Text(
                text = "JeanFit",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = SunsetOrange,
                modifier = Modifier.alpha(titleAlpha.value)
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Dein personalisierter Weg zu einem gesunden Lebensstil",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.alpha(taglineAlpha.value)
        )

        Spacer(Modifier.height(48.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it; viewModel.setName(it) },
            label = { Text("Dein Name") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(fieldAlpha.value),
            shape = MaterialTheme.shapes.large
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onNext,
            enabled = name.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .alpha(buttonAlpha.value),
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
