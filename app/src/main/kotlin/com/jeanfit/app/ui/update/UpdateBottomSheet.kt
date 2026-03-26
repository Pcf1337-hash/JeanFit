package com.jeanfit.app.ui.update

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeanfit.app.domain.update.ReleaseInfo
import com.jeanfit.app.ui.theme.CoachCardDark
import com.jeanfit.app.ui.theme.JeanFitTheme
import com.jeanfit.app.ui.theme.MidnightBlue
import com.jeanfit.app.ui.theme.OceanBlue
import com.jeanfit.app.ui.theme.SkyBlue
import com.jeanfit.app.ui.theme.StreakFire

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateBottomSheet(
    release: ReleaseInfo,
    isDownloading: Boolean,
    downloadProgress: Int,          // 0–100
    currentVersion: String,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit,
    onSkip: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Forced Update → Back-Button sperren
    if (release.isForced) {
        BackHandler(enabled = true) { /* blockieren */ }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!release.isForced && !isDownloading) onDismiss() },
        sheetState = sheetState,
        containerColor = CoachCardDark,
        tonalElevation = 0.dp,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            )
            Spacer(Modifier.height(20.dp))

            // Icon + Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Filled.SystemUpdate,
                    contentDescription = null,
                    tint = SkyBlue,
                    modifier = Modifier.size(28.dp)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SkyBlue.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        "Update verfügbar",
                        color = SkyBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (release.isForced) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(StreakFire.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            "Pflicht",
                            color = StreakFire,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // Version-Zeile: alt → neu
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    currentVersion,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.width(10.dp))
                Icon(
                    Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = SkyBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    release.versionName,
                    color = SkyBlue,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                release.title,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))

            // Changelog Box
            if (release.changelog.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MidnightBlue.copy(alpha = 0.6f))
                        .padding(14.dp)
                        .height(160.dp)
                ) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        ChangelogContent(release.changelog)
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Download-Progress
            if (isDownloading) {
                val animatedProgress by animateFloatAsState(
                    targetValue = downloadProgress / 100f,
                    animationSpec = tween(300),
                    label = "progress"
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        if (downloadProgress > 0) "Wird heruntergeladen... $downloadProgress%"
                        else "Wird vorbereitet...",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp)),
                        color = SkyBlue,
                        trackColor = Color.White.copy(alpha = 0.15f)
                    )
                }
                Spacer(Modifier.height(20.dp))
            }

            // Update-Button
            Button(
                onClick = { if (!isDownloading) onUpdate() },
                enabled = !isDownloading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OceanBlue,
                    disabledContainerColor = OceanBlue.copy(alpha = 0.5f)
                )
            ) {
                Text(
                    if (isDownloading) "Wird geladen..." else "Jetzt updaten",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Später / Überspringen (nur wenn nicht forced)
            if (!release.isForced) {
                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isDownloading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White.copy(alpha = 0.7f)
                    )
                ) {
                    Text("Später erinnern")
                }
                Spacer(Modifier.height(4.dp))
                TextButton(
                    onClick = onSkip,
                    enabled = !isDownloading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Version ${release.versionName} überspringen",
                        color = Color.White.copy(alpha = 0.35f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ChangelogContent(markdown: String) {
    val lines = markdown.lines()
        .filter { !it.matches(Regex("FORCED_UPDATE:.*", RegexOption.IGNORE_CASE)) }

    lines.forEach { line ->
        when {
            line.startsWith("## ") || line.startsWith("### ") -> {
                Text(
                    line.trimStart('#', ' '),
                    color = SkyBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                )
            }
            line.startsWith("- ") || line.startsWith("* ") -> {
                Text(
                    "  • ${line.drop(2)}",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
            line.isNotBlank() -> {
                Text(
                    line,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A1929)
@Composable
private fun UpdateBottomSheetPreview() {
    JeanFitTheme(darkTheme = true) {
        UpdateBottomSheet(
            release = ReleaseInfo(
                versionName = "1.2.0",
                versionCode = 3,
                tagName = "v1.2.0",
                title = "KI-Coach Erweiterung & Ocean Blue Design",
                changelog = "## Was ist neu\n- Jean KI-Coach mit echtem Claude API\n- Ocean Blue Design System\n- Langzeit-Gedächtnis für den Coach\n\n## Bugfixes\n- Diverse kleinere Korrekturen",
                publishedAt = "2026-03-26",
                downloadUrl = "",
                isForced = false
            ),
            isDownloading = false,
            downloadProgress = 0,
            currentVersion = "1.1.0",
            onUpdate = {},
            onDismiss = {},
            onSkip = {}
        )
    }
}
