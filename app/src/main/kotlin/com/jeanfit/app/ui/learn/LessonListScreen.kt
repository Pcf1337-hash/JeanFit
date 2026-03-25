package com.jeanfit.app.ui.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeanfit.app.ui.theme.*

@Composable
fun LessonListScreen(
    courseId: String,
    onBack: () -> Unit,
    onLessonSelected: (String) -> Unit,
    viewModel: LearnViewModel = hiltViewModel()
) {
    LaunchedEffect(courseId) { viewModel.selectCourse(courseId) }
    val state by viewModel.lessonListState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.course?.title ?: "Lektionen", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Zurück") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SunsetOrange)
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.course?.description?.let {
                item {
                    Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
                }
            }
            items(state.lessons) { lesson ->
                val isCompleted = lesson.lessonId in state.completedIds
                Card(
                    onClick = { onLessonSelected(lesson.lessonId) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Filled.PlayCircle,
                            contentDescription = null,
                            tint = if (isCompleted) FoodGreen else SunsetOrange,
                            modifier = Modifier.size(32.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(lesson.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text(
                                "${lesson.estimatedMinutes} Min • ${lesson.lessonType.replaceFirstChar { it.uppercaseChar() }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isCompleted) {
                            Text("✓", color = FoodGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LessonListPreview() {
    JeanFitTheme { LessonListScreen(courseId = "course_1", onBack = {}, onLessonSelected = {}) }
}
