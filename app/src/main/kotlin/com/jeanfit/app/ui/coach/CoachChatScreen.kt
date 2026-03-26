package com.jeanfit.app.ui.coach

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeanfit.app.data.db.entities.CoachMessage
import com.jeanfit.app.ui.theme.CoachCardDark
import com.jeanfit.app.ui.theme.DeepNavy
import com.jeanfit.app.ui.theme.JeanFitTheme
import com.jeanfit.app.ui.theme.MidnightBlue
import com.jeanfit.app.ui.theme.OceanBlue
import com.jeanfit.app.ui.theme.SkyBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val QUICK_REPLIES = listOf(
    "💧 Ich habe Hunger",
    "😔 Ich bin demotiviert",
    "📊 Wie läuft es?",
    "🍽️ Rezeptvorschlag",
    "⚖️ Ich stecke auf einem Plateau",
    "🏃 Sport-Tipp",
    "😰 Ich hatte Stress",
    "🎉 Ich habe mein Ziel erreicht!"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachChatScreen(
    onBack: () -> Unit = {},
    viewModel: CoachViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.markAllRead()
    }

    Scaffold(
        topBar = {
            CoachTopBar(onClearChat = viewModel::clearChat)
        },
        containerColor = MidnightBlue
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            // Nachrichten-Liste
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.messages, key = { it.id }) { message ->
                    MessageBubble(message = message)
                }
                if (uiState.isLoading) {
                    item { TypingIndicator() }
                }
            }

            // Quick-Reply Chips
            AnimatedVisibility(
                visible = !uiState.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(QUICK_REPLIES) { reply ->
                        AssistChip(
                            onClick = { viewModel.sendQuickReply(reply) },
                            label = { Text(reply, fontSize = 12.sp) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = CoachCardDark,
                                labelColor = SkyBlue
                            ),
                            border = AssistChipDefaults.assistChipBorder(
                                enabled = true,
                                borderColor = SkyBlue.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }

            // Eingabe-Zeile
            InputRow(
                text = uiState.inputText,
                isLoading = uiState.isLoading,
                onTextChange = viewModel::onInputChange,
                onSend = viewModel::sendMessage
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoachTopBar(onClearChat: () -> Unit) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Jean-Avatar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(listOf(SkyBlue, OceanBlue))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("J", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        "Jean",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Dein persönlicher Coach",
                        style = MaterialTheme.typography.labelSmall,
                        color = SkyBlue
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onClearChat) {
                Icon(
                    Icons.Filled.DeleteSweep,
                    contentDescription = "Chat leeren",
                    tint = SkyBlue
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = CoachCardDark
        )
    )
}

@Composable
private fun MessageBubble(message: CoachMessage) {
    val isCoach = message.isFromCoach
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCoach) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.Bottom
    ) {
        if (isCoach) {
            // Jean-Avatar
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(SkyBlue, OceanBlue))),
                contentAlignment = Alignment.Center
            ) {
                Text("J", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(6.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isCoach) Alignment.Start else Alignment.End
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = if (isCoach) 4.dp else 16.dp,
                            topEnd = 16.dp,
                            bottomEnd = if (isCoach) 16.dp else 4.dp,
                            bottomStart = 16.dp
                        )
                    )
                    .background(
                        if (isCoach) CoachCardDark
                        else OceanBlue
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.content,
                    color = if (isCoach) Color(0xFFE2E8F4) else Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = formatTimestamp(message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8E9099),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
private fun TypingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.padding(start = 34.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 16.dp)
                )
                .background(CoachCardDark)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) { index ->
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = index * 200),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dot_$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(SkyBlue.copy(alpha = alpha))
                    )
                }
            }
        }
    }
}

@Composable
private fun InputRow(
    text: String,
    isLoading: Boolean,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CoachCardDark)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text("Frag Jean etwas...", color = Color(0xFF8E9099))
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SkyBlue,
                unfocusedBorderColor = Color(0xFF2A5298),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = SkyBlue,
                focusedContainerColor = Color(0xFF1E3A5F),
                unfocusedContainerColor = Color(0xFF1A2D45)
            ),
            shape = RoundedCornerShape(24.dp),
            maxLines = 4,
            enabled = !isLoading
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (text.isNotBlank() && !isLoading) OceanBlue
                    else Color(0xFF2A3F5F)
                ),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onSend,
                enabled = text.isNotBlank() && !isLoading
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Senden",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.GERMANY)
    return sdf.format(Date(timestamp))
}

@Preview(showBackground = true, backgroundColor = 0xFF0A1929)
@Composable
private fun CoachChatScreenPreview() {
    JeanFitTheme(darkTheme = true) {
        CoachChatScreen()
    }
}
