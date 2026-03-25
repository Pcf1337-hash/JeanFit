package com.jeanfit.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeanfit.app.ui.theme.*
import kotlin.math.min

@Composable
fun HomeScreen(
    onLogFood: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = SunsetOrange)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            // Header
            Surface(
                color = SunsetOrange,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp).statusBarsPadding()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Hallo, ${state.profile?.name?.split(" ")?.firstOrNull() ?: "du"}!",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                "Wie läuft's heute?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Filled.MonetizationOn,
                                contentDescription = "Coins",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                "${state.coins}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            // Calorie Ring
            CalorieRingCard(
                consumed = state.todayCalories,
                goal = state.profile?.dailyCalorieGoal?.toFloat() ?: 1600f
            )
        }

        item {
            // Meal sections
            Text(
                "Mahlzeiten",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        val mealTypes = listOf("breakfast" to "Frühstück", "lunch" to "Mittagessen", "dinner" to "Abendessen", "snack" to "Snack")
        items(mealTypes) { (mealType, label) ->
            val entries = state.todayEntries.filter { it.mealType == mealType }
            MealCard(
                mealType = mealType,
                label = label,
                entries = entries,
                onAdd = { onLogFood(mealType) }
            )
        }

        item {
            // Daily Tasks
            DailyTasksCard(
                task = state.dailyTask,
                onFinishDay = viewModel::finishDay
            )
        }
    }
}

@Composable
private fun CalorieRingCard(consumed: Float, goal: Float) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(120.dp)
            ) {
                val progress = min(1f, if (goal > 0) consumed / goal else 0f)
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 10.dp,
                    strokeCap = StrokeCap.Round
                )
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = SunsetOrange,
                    strokeWidth = 10.dp,
                    strokeCap = StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${consumed.toInt()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SunsetOrange
                    )
                    Text("kcal", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CalorieStat("Ziel", "${goal.toInt()} kcal", SunsetOrange)
                CalorieStat("Verbraucht", "${consumed.toInt()} kcal", FoodOrange)
                CalorieStat("Verbleibend", "${(goal - consumed).coerceAtLeast(0f).toInt()} kcal", FoodGreen)
            }
        }
    }
}

@Composable
private fun CalorieStat(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier.size(10.dp).background(color, shape = MaterialTheme.shapes.extraSmall)
        )
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun MealCard(
    mealType: String,
    label: String,
    entries: List<com.jeanfit.app.data.db.entities.FoodLogEntry>,
    onAdd: () -> Unit
) {
    val totalCal = entries.sumOf { it.calories.toDouble() }.toInt()
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (totalCal > 0) {
                        Text("$totalCal kcal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(8.dp))
                    }
                    IconButton(onClick = onAdd, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.Add, contentDescription = "Hinzufügen", tint = SunsetOrange)
                    }
                }
            }
            if (entries.isEmpty()) {
                Text(
                    "Noch nichts eingetragen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                entries.forEach { entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            entry.colorCategory.toEmoji() + " " + entry.foodId.take(20),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${entry.calories.toInt()} kcal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun String.toEmoji(): String = when (this) {
    "green" -> "🟢"
    "yellow" -> "🟡"
    "orange" -> "🟠"
    else -> "⬜"
}

@Composable
private fun DailyTasksCard(
    task: com.jeanfit.app.data.db.entities.DailyTask?,
    onFinishDay: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tagesaufgaben", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                if (task?.coinAwarded == true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = FoodYellow, modifier = Modifier.size(16.dp))
                        Text(" +1 Coin!", style = MaterialTheme.typography.labelSmall, color = FoodYellow)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            TaskRow("Gewicht wiegen", task?.weightLogged == true)
            TaskRow("Mahlzeiten loggen", task?.allMealsLogged == true)
            TaskRow("Lektion lesen", task?.lessonCompleted == true)
            if (task?.allMealsLogged == false) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onFinishDay,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SunsetOrange)
                ) {
                    Text("Tag abschließen")
                }
            }
        }
    }
}

@Composable
private fun TaskRow(label: String, done: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (done) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (done) FoodGreen else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomePreview() {
    JeanFitTheme { HomeScreen(onLogFood = {}) }
}
