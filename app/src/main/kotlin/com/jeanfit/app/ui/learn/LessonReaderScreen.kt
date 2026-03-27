package com.jeanfit.app.ui.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeanfit.app.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun LessonReaderScreen(
    lessonId: String,
    onBack: () -> Unit,
    viewModel: LearnViewModel = hiltViewModel()
) {
    LaunchedEffect(lessonId) { viewModel.selectLesson(lessonId) }
    val state by viewModel.lessonReaderState.collectAsState()
    var quizAnswers by remember { mutableStateOf(mapOf<Int, Int>()) }
    var quizFeedback by remember { mutableStateOf(mapOf<Int, Boolean>()) }
    val listState = rememberLazyListState()

    // Calculate scroll progress for the reading progress indicator
    val scrollProgress by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) return@derivedStateOf 0f
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            (lastVisibleIndex + 1).toFloat() / totalItems.toFloat()
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(state.lesson?.title ?: "Lektion", fontWeight = FontWeight.SemiBold)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, "Zurück")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                // Reading progress bar
                LinearProgressIndicator(
                    progress = { scrollProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = Color(0xFF1565C0),
                    trackColor = Color(0xFF1E3A5F)
                )
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OceanBlue)
            }
            return@Scaffold
        }
        val lesson = state.lesson ?: return@Scaffold
        val blocks = parseContentBlocks(lesson.contentJson)

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(blocks) { index, block ->
                when (block.type) {
                    "text" -> TextBlock(content = block.content)
                    "tip" -> TipBlock(icon = block.icon ?: "💡", content = block.content)
                    "quiz" -> QuizBlock(
                        question = block.question ?: "",
                        options = block.options ?: emptyList(),
                        correctIndex = block.correctIndex ?: 0,
                        explanation = block.explanation ?: "",
                        selectedIndex = quizAnswers[index],
                        feedback = quizFeedback[index],
                        onAnswer = { answerIndex ->
                            quizAnswers = quizAnswers + (index to answerIndex)
                            quizFeedback = quizFeedback + (index to (answerIndex == (block.correctIndex ?: 0)))
                        }
                    )
                    "activity" -> ActivityBlock(title = block.title ?: "", instruction = block.content)
                    "reflection" -> ReflectionBlock(prompt = block.prompt ?: "")
                    else -> {}
                }
            }

            item {
                if (state.isCompleted) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = FoodGreen.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.CheckCircle, null, tint = FoodGreen)
                            Text(
                                "Lektion abgeschlossen!",
                                color = FoodGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    // Gradient button for completing the lesson
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF1565C0), Color(0xFF00BCD4))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = {
                                viewModel.completeLesson(lessonId, lesson.coinsReward)
                            },
                            modifier = Modifier.fillMaxSize(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp)
                        ) {
                            Text(
                                if (lesson.coinsReward > 0) "Abschließen (+${lesson.coinsReward} Coins)"
                                else "Lektion abschließen",
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TextBlock(content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Text(
            text = content.replace("##", "").replace("**", "").trim(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp),
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
    }
}

@Composable
private fun TipBlock(icon: String, content: String) {
    // OceanBlue left bar, DarkSurface2 background
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A2E45), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .defaultMinSize(minHeight = 40.dp)
                .background(Color(0xFF1565C0), RoundedCornerShape(2.dp))
        )
        Text(icon, style = MaterialTheme.typography.titleMedium)
        Text(
            content.replace("**", "").trim(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuizBlock(
    question: String,
    options: List<String>,
    correctIndex: Int,
    explanation: String,
    selectedIndex: Int?,
    feedback: Boolean?,
    onAnswer: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Quiz",
                style = MaterialTheme.typography.labelMedium,
                color = OceanBlue,
                fontWeight = FontWeight.Bold
            )
            Text(question, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            options.forEachIndexed { index, option ->
                val isSelected = selectedIndex == index
                val isCorrect = index == correctIndex
                val bgColor = when {
                    selectedIndex == null -> MaterialTheme.colorScheme.surfaceVariant
                    isSelected && feedback == true -> FoodGreen.copy(alpha = 0.2f)
                    isSelected && feedback == false -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    selectedIndex != null && isCorrect -> FoodGreen.copy(alpha = 0.1f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                Surface(
                    onClick = { if (selectedIndex == null) onAnswer(index) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = bgColor
                ) {
                    Text(
                        "${('A' + index)}. $option",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            if (feedback != null) {
                val msg = if (feedback) "✓ Richtig! $explanation" else "✗ Leider falsch. $explanation"
                val color = if (feedback) FoodGreen else MaterialTheme.colorScheme.error
                Text(msg, style = MaterialTheme.typography.bodySmall, color = color)
            }
        }
    }
}

@Composable
private fun ActivityBlock(title: String, instruction: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = OceanBlue.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("🎯 $title", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(instruction, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ReflectionBlock(prompt: String) {
    var text by remember { mutableStateOf("") }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "💭 Reflektion",
                style = MaterialTheme.typography.labelMedium,
                color = DeepNavy,
                fontWeight = FontWeight.Bold
            )
            Text(prompt, style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Deine Gedanken...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1565C0),
                    focusedLabelColor = Color(0xFF1565C0),
                    cursorColor = Color(0xFF1565C0)
                )
            )
        }
    }
}

private data class ContentBlock(
    val type: String,
    val content: String = "",
    val icon: String? = null,
    val question: String? = null,
    val options: List<String>? = null,
    val correctIndex: Int? = null,
    val explanation: String? = null,
    val title: String? = null,
    val prompt: String? = null
)

private fun parseContentBlocks(json: String): List<ContentBlock> {
    return try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            ContentBlock(
                type = obj.optString("type"),
                content = obj.optString("content"),
                icon = obj.optString("icon").ifBlank { null },
                question = obj.optString("question").ifBlank { null },
                options = if (obj.has("options")) {
                    val opts = obj.getJSONArray("options")
                    (0 until opts.length()).map { opts.getString(it) }
                } else null,
                correctIndex = if (obj.has("correctIndex")) obj.getInt("correctIndex") else null,
                explanation = obj.optString("explanation").ifBlank { null },
                title = obj.optString("title").ifBlank { null },
                prompt = obj.optString("prompt").ifBlank { null }
            )
        }
    } catch (e: Exception) { emptyList() }
}

@Preview(showBackground = true)
@Composable
private fun LessonReaderPreview() {
    JeanFitTheme { LessonReaderScreen(lessonId = "l1_1", onBack = {}) }
}
