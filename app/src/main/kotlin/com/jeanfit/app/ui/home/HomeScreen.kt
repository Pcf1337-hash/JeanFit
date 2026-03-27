package com.jeanfit.app.ui.home

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.jeanfit.app.data.db.entities.FoodLogEntry
import com.jeanfit.app.ui.components.CalorieRingProgress
import com.jeanfit.app.ui.theme.*
import com.jeanfit.app.ui.update.UpdateViewModel
import kotlin.math.min

@Composable
fun HomeScreen(
    onLogFood: (String) -> Unit,
    onSettings: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val activity = LocalActivity.current as? androidx.activity.ComponentActivity
    val updateViewModel: UpdateViewModel? = if (activity != null) {
        hiltViewModel(viewModelStoreOwner = activity)
    } else null
    val updateState by (updateViewModel?.state?.collectAsState() ?: remember { mutableStateOf(null) })

    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = OceanBlue)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Gradient Header
        item {
            GradientHeader(
                name = state.profile?.name?.split(" ")?.firstOrNull() ?: "du",
                coins = state.coins,
                onSettings = onSettings,
                updateViewModel = updateViewModel,
                isChecking = updateState?.isChecking == true
            )
        }

        // Calorie Ring Card
        item {
            CalorieRingCard(
                consumed = state.todayCalories,
                goal = state.profile?.dailyCalorieGoal?.toFloat() ?: 1600f
            )
        }

        // Streak Banner (wenn streak >= 3)
        val streak = state.dailyTask?.let { 0 } ?: 0 // stub: kein streak-Feld in UiState
        if (streak >= 3) {
            item {
                StreakBanner(streak = streak)
            }
        }

        // Daily Tasks Card
        item {
            DailyTasksCard(
                task = state.dailyTask,
                onFinishDay = viewModel::finishDay
            )
        }

        // Meal Slots Header
        item {
            Text(
                "Mahlzeiten",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        val mealTypes = listOf(
            Triple("breakfast", "Frühstück", "🌅"),
            Triple("lunch", "Mittagessen", "🌞"),
            Triple("dinner", "Abendessen", "🌙"),
            Triple("snack", "Snack", "🍎")
        )
        items(mealTypes) { (mealType, label, emoji) ->
            val entries = state.todayEntries.filter { it.mealType == mealType }
            val mealCalorieGoal = (state.profile?.dailyCalorieGoal?.toFloat() ?: 1600f) / 4f
            MealCard(
                mealType = mealType,
                label = label,
                emoji = emoji,
                entries = entries,
                mealCalorieGoal = mealCalorieGoal,
                onAdd = { onLogFood(mealType) },
                onDelete = viewModel::deleteLogEntry,
                onEdit = { entry, multiplier -> viewModel.updateLogEntryServings(entry, multiplier) }
            )
        }
    }
}

// ─── Gradient Header ────────────────────────────────────────────────────────

@Composable
private fun GradientHeader(
    name: String,
    coins: Int,
    onSettings: () -> Unit,
    updateViewModel: UpdateViewModel?,
    isChecking: Boolean
) {
    val hour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val greeting = when {
        hour < 12 -> "Guten Morgen"
        hour < 18 -> "Guten Tag"
        else -> "Guten Abend"
    }

    val dateFormatter = remember {
        java.time.format.DateTimeFormatter.ofPattern("EEEE, d. MMMM", java.util.Locale.GERMAN)
    }
    val todayDate = remember { java.time.LocalDate.now().format(dateFormatter) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(OceanBlue, DeepNavy)))
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Greeting + Date
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$greeting, $name!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = todayDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                // Icons: Coins, Settings, Update
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Coin display
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Filled.MonetizationOn,
                            contentDescription = "Coins",
                            tint = CoinGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "$coins",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Settings
                    IconButton(onClick = onSettings) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Einstellungen",
                            tint = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    // Update check
                    if (updateViewModel != null) {
                        IconButton(
                            onClick = { updateViewModel.checkForUpdateManually() },
                            enabled = !isChecking
                        ) {
                            Icon(
                                if (isChecking) Icons.Filled.Sync else Icons.Filled.SystemUpdate,
                                contentDescription = "Update prüfen",
                                tint = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Calorie Ring Card ───────────────────────────────────────────────────────

@Composable
private fun CalorieRingCard(consumed: Float, goal: Float) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Animated CalorieRingProgress from shared component
            CalorieRingProgress(
                consumed = consumed.toInt(),
                goal = goal.toInt(),
                size = 130.dp,
                strokeWidth = 12.dp
            )

            // Stats
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                CalorieStat(
                    label = "Ziel",
                    value = "${goal.toInt()} kcal",
                    color = OceanBlue
                )
                CalorieStat(
                    label = "Verbraucht",
                    value = "${consumed.toInt()} kcal",
                    color = TealAccent
                )
                CalorieStat(
                    label = "Verbleibend",
                    value = "${(goal - consumed).coerceAtLeast(0f).toInt()} kcal",
                    color = FoodGreen
                )
            }
        }
    }
}

@Composable
private fun CalorieStat(label: String, value: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, shape = RoundedCornerShape(2.dp))
        )
        Column {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ─── Streak Banner ───────────────────────────────────────────────────────────

@Composable
private fun StreakBanner(streak: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(StreakFire, Color(0xFFFF9500))
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("🔥", style = MaterialTheme.typography.titleLarge)
            Column {
                Text(
                    "$streak Tage in Folge!",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Du bist auf Feuer – weiter so!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

// ─── Daily Tasks Card ────────────────────────────────────────────────────────

@Composable
private fun DailyTasksCard(
    task: com.jeanfit.app.data.db.entities.DailyTask?,
    onFinishDay: () -> Unit
) {
    val doneCount = listOf(
        task?.weightLogged == true,
        task?.allMealsLogged == true,
        task?.lessonCompleted == true
    ).count { it }
    val allDone = doneCount == 3

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Tagesaufgaben",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                // Progress counter + Coin badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "$doneCount/3",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (allDone) OceanBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    if (task?.coinAwarded == true) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(CoinGold.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Filled.MonetizationOn, null, tint = CoinGold, modifier = Modifier.size(14.dp))
                            Text("+1 Coin!", style = MaterialTheme.typography.labelSmall, color = CoinGold, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { doneCount / 3f },
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                color = if (allDone) OceanBlue else TealAccent,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )

            Spacer(Modifier.height(12.dp))

            AnimatedTaskRow(label = "Gewicht wiegen",    done = task?.weightLogged == true)
            Spacer(Modifier.height(4.dp))
            AnimatedTaskRow(label = "Mahlzeiten loggen", done = task?.allMealsLogged == true)
            Spacer(Modifier.height(4.dp))
            AnimatedTaskRow(label = "Lektion lesen",     done = task?.lessonCompleted == true)

            // Abschluss-Button: nur sichtbar wenn Mahlzeiten noch nicht bestätigt
            if (task?.allMealsLogged == false) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onFinishDay,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlue)
                ) {
                    Icon(Icons.Filled.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Mahlzeiten abschließen")
                }
                if (doneCount < 2) {
                    // Hinweis: andere Aufgaben noch offen
                    Text(
                        text = "Noch ${3 - doneCount} Aufgabe${if (3 - doneCount != 1) "n" else ""} offen für den Noomcoin",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else if (allDone && task?.coinAwarded == false) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Alle Aufgaben erledigt – Coin wird vergeben!",
                    style = MaterialTheme.typography.labelSmall,
                    color = OceanBlue,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun AnimatedTaskRow(label: String, done: Boolean) {
    val iconTint by animateColorAsState(
        targetValue = if (done) OceanBlue else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 400),
        label = "taskIconTint_$label"
    )
    val textColor by animateColorAsState(
        targetValue = if (done) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 400),
        label = "taskTextColor_$label"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (done) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = if (done) "Erledigt" else "Offen",
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
    }
}

// ─── Meal Card ───────────────────────────────────────────────────────────────

@Composable
private fun MealCard(
    mealType: String,
    label: String,
    emoji: String,
    entries: List<FoodLogEntry>,
    mealCalorieGoal: Float,
    onAdd: () -> Unit,
    onDelete: (Long) -> Unit,
    onEdit: (FoodLogEntry, Float) -> Unit
) {
    val totalCal = entries.sumOf { it.calories.toDouble() }.toFloat()
    val progress = min(1f, if (mealCalorieGoal > 0) totalCal / mealCalorieGoal else 0f)
    var editingEntry by remember { mutableStateOf<FoodLogEntry?>(null) }

    // Edit Dialog
    editingEntry?.let { entry ->
        var multiplier by remember { mutableFloatStateOf(entry.servingMultiplier) }
        AlertDialog(
            onDismissRequest = { editingEntry = null },
            title = {
                Text(
                    entry.foodName.ifBlank { "Eintrag bearbeiten" },
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Portionsmenge: ${"%.1f".format(multiplier)}× (${(entry.servingSizeG / entry.servingMultiplier * multiplier).toInt()} g)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = multiplier,
                        onValueChange = { multiplier = it },
                        valueRange = 0.25f..5f,
                        steps = 18,
                        colors = SliderDefaults.colors(
                            thumbColor = OceanBlue,
                            activeTrackColor = OceanBlue
                        )
                    )
                    Text(
                        "≈ ${(entry.calories / entry.servingMultiplier * multiplier).toInt()} kcal",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = OceanBlue
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { onEdit(entry, multiplier); editingEntry = null }) {
                    Text("Speichern", color = OceanBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingEntry = null }) { Text("Abbrechen") }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(emoji, style = MaterialTheme.typography.titleMedium)
                    Text(
                        label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (totalCal > 0) {
                        Text(
                            "${totalCal.toInt()} kcal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    IconButton(
                        onClick = onAdd,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Hinzufügen",
                            tint = OceanBlue
                        )
                    }
                }
            }

            // LinearProgressIndicator
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = OceanBlue,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Empty state
            if (entries.isEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Noch nichts eingetragen",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Spacer(Modifier.height(8.dp))
                entries.forEach { entry ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                onDelete(entry.id); true
                            } else false
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Löschen",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                entry.colorCategory.toEmoji() + " " + entry.foodName.ifBlank { entry.foodId.take(20) },
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${entry.calories.toInt()} kcal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                IconButton(
                                    onClick = { editingEntry = entry },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = "Bearbeiten",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun String.toEmoji(): String = when (this) {
    "green"  -> "🟢"
    "yellow" -> "🟡"
    "orange" -> "🟠"
    else     -> "⬜"
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "HomeScreen Light")
@Composable
private fun HomePreviewLight() {
    JeanFitTheme(darkTheme = false) {
        HomeScreen(onLogFood = {})
    }
}

@Preview(showBackground = true, name = "HomeScreen Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomePreviewDark() {
    JeanFitTheme(darkTheme = true) {
        HomeScreen(onLogFood = {})
    }
}
