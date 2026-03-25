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
            TopAppBar(
                title = { Text("Lernen", fontWeight = FontWeight.Bold) },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = FoodYellow)
                        Text("${state.coins}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                },
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

        if (state.courses.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SunsetOrange)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Dein Lernpfad",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(state.courses) { course ->
                val isLocked = state.coins < course.requiredCoinsToUnlock
                val completedInCourse = state.completedLessonIds.count { lessonId ->
                    lessonId.startsWith(course.courseId.replace("course_", "l"))
                }
                CourseCard(
                    course = course,
                    isLocked = isLocked,
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
    completedCount: Int,
    onClick: () -> Unit
) {
    val progress = if (course.totalLessons > 0) completedCount.toFloat() / course.totalLessons.toFloat() else 0f
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(course.iconEmoji, fontSize = 36.sp)
                    Column {
                        Text(
                            "Woche ${course.weekNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant else SunsetOrange
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
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Text("${course.requiredCoinsToUnlock}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(course.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (!isLocked) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = SunsetOrange,
                    trackColor = SunsetOrange.copy(alpha = 0.2f)
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

@Preview(showBackground = true)
@Composable
private fun CourseMapPreview() {
    JeanFitTheme { CourseMapScreen(onCourseSelected = {}) }
}
