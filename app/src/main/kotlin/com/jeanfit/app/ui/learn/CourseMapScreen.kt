package com.jeanfit.app.ui.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeanfit.app.data.db.entities.Course
import com.jeanfit.app.ui.theme.*

@Composable
fun CourseMapScreen(
    onCourseSelected: (String) -> Unit,
    viewModel: LearnViewModel = hiltViewModel()
) {
    val state by viewModel.learnState.collectAsState()

    Scaffold(
        topBar = {
            // Gradient header replacing plain TopAppBar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color(0xFF1565C0), Color(0xFF0D2B4E))))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Lernpfad",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = CoinGold)
                        Text(
                            "${state.coins}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OceanBlue)
            }
            return@Scaffold
        }

        if (state.courses.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = OceanBlue)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(state.courses) { course ->
                val isLocked = state.coins < course.requiredCoinsToUnlock
                val completedInCourse = state.completedLessonIds.count { lessonId ->
                    lessonId.startsWith(course.courseId.replace("course_", "l"))
                }
                val isCompleted = completedInCourse >= course.totalLessons && course.totalLessons > 0
                CourseCard(
                    course = course,
                    isLocked = isLocked,
                    isCompleted = isCompleted,
                    completedCount = completedInCourse,
                    onClick = { if (!isLocked) onCourseSelected(course.courseId) }
                )
            }
        }
    }
}

@Composable
private fun CourseCard(
    course: Course,
    isLocked: Boolean,
    isCompleted: Boolean,
    completedCount: Int,
    onClick: () -> Unit
) {
    val progress = if (course.totalLessons > 0) completedCount.toFloat() / course.totalLessons.toFloat() else 0f

    val sideBarColor = when {
        isLocked -> Color.Gray
        isCompleted -> Color(0xFF00E676)
        else -> Color(0xFF1565C0)
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left colored bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .defaultMinSize(minHeight = 80.dp)
                    .fillMaxHeight()
                    .background(sideBarColor)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(course.iconEmoji, fontSize = 36.sp)
                        Column {
                            Text(
                                "Woche ${course.weekNumber}",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant else OceanBlue
                            )
                            Text(
                                course.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    if (isLocked) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = "Gesperrt",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "${course.requiredCoinsToUnlock}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    course.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!isLocked) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = if (isCompleted) Color(0xFF00E676) else OceanBlue,
                        trackColor = OceanBlue.copy(alpha = 0.2f)
                    )
                    Text(
                        "$completedCount/${course.totalLessons} Lektionen",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseMapPreview() {
    JeanFitTheme { CourseMapScreen(onCourseSelected = {}) }
}
