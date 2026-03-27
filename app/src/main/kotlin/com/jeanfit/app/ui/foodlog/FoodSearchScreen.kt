package com.jeanfit.app.ui.foodlog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jeanfit.app.data.db.entities.FoodItem
import com.jeanfit.app.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun FoodSearchScreen(
    mealType: String,
    onBack: () -> Unit,
    onScanBarcode: (String) -> Unit,
    viewModel: FoodSearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val mealLabel = when (mealType) {
        "breakfast" -> "Frühstück"
        "lunch" -> "Mittagessen"
        "dinner" -> "Abendessen"
        else -> "Snack"
    }

    LaunchedEffect(state.logSuccess) {
        if (state.logSuccess) onBack()
    }

    if (state.selectedItem != null) {
        FoodDetailSheet(
            item = state.selectedItem!!,
            servingSize = state.servingSize,
            mealType = mealType,
            onServingChange = viewModel::setServingSize,
            onLog = { viewModel.logFood(mealType) },
            onDismiss = viewModel::clearSelection,
            isLogging = state.isLogging
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(mealLabel, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Zurück") }
                },
                actions = {
                    IconButton(onClick = { onScanBarcode(mealType) }) {
                        Icon(
                            Icons.Filled.QrCodeScanner,
                            contentDescription = "Barcode scannen",
                            tint = OceanBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // Search field with OceanBlue focus border
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                placeholder = { Text("Lebensmittel suchen...") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = {
                    if (state.isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = OceanBlue
                        )
                    } else if (state.query.isNotBlank()) {
                        IconButton(onClick = { viewModel.setQuery("") }) {
                            Icon(Icons.Filled.Clear, null)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1565C0),
                    focusedLabelColor = Color(0xFF1565C0),
                    cursorColor = Color(0xFF1565C0)
                )
            )

            if (state.query.isBlank()) {
                // Recent items
                if (state.recentItems.isNotEmpty()) {
                    Text(
                        "Zuletzt verwendet",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    LazyColumn {
                        items(state.recentItems) { item ->
                            FoodItemRow(item = item, onClick = { viewModel.selectItem(item) })
                        }
                        item {
                            AddCustomFoodButton(onAdd = { name, cal, p, c, f ->
                                viewModel.addCustomFood(name, cal, p, c, f)
                            })
                        }
                    }
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.Search,
                                null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Suche nach Lebensmitteln",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                val allResults = (state.localResults + state.remoteResults).distinctBy { it.foodId }
                if (allResults.isEmpty() && !state.isSearching) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Keine Ergebnisse für \"${state.query}\"",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            TextButton(onClick = {}) { Text("Eigenes Lebensmittel erstellen") }
                        }
                    }
                } else {
                    LazyColumn {
                        items(allResults) { item ->
                            FoodItemRow(item = item, onClick = { viewModel.selectItem(item) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodItemRow(item: FoodItem, onClick: () -> Unit) {
    val categoryColor = when (item.colorCategory) {
        "green" -> FoodGreen
        "yellow" -> FoodYellow
        "orange" -> FoodOrange
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left color dot (category indicator on the left)
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(categoryColor, shape = MaterialTheme.shapes.extraSmall)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            if (item.brand != null) {
                Text(
                    item.brand,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(
            "${item.caloriesPer100g.roundToInt()} kcal/100g",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // Right color dot indicator
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(categoryColor, CircleShape)
        )
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
}

@Composable
internal fun FoodDetailSheet(
    item: FoodItem,
    servingSize: Float,
    mealType: String,
    onServingChange: (Float) -> Unit,
    onLog: () -> Unit,
    onDismiss: () -> Unit,
    isLogging: Boolean
) {
    val factor = servingSize / 100f
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(item.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                item.brand?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = servingSize.roundToInt().toString(),
                    onValueChange = { it.toFloatOrNull()?.let(onServingChange) },
                    label = { Text("Menge (${item.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1565C0),
                        focusedLabelColor = Color(0xFF1565C0),
                        cursorColor = Color(0xFF1565C0)
                    )
                )
                Slider(
                    value = servingSize,
                    onValueChange = onServingChange,
                    valueRange = 10f..500f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(thumbColor = OceanBlue, activeTrackColor = OceanBlue)
                )
                NutrientRow("Kalorien", "${(item.caloriesPer100g * factor).roundToInt()} kcal", OceanBlue)
                NutrientRow("Protein", "${(item.proteinPer100g * factor).roundToInt()} g", FoodGreen)
                NutrientRow("Kohlenhydrate", "${(item.carbsPer100g * factor).roundToInt()} g", FoodYellow)
                NutrientRow("Fett", "${(item.fatPer100g * factor).roundToInt()} g", FoodOrange)
            }
        },
        confirmButton = {
            Button(
                onClick = onLog,
                enabled = !isLogging,
                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue)
            ) {
                if (isLogging) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Eintragen")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}

@Composable
private fun NutrientRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(modifier = Modifier.size(8.dp).background(color, MaterialTheme.shapes.extraSmall))
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun AddCustomFoodButton(onAdd: (String, Float, Float, Float, Float) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        var name by remember { mutableStateOf("") }
        var cal by remember { mutableStateOf("") }
        var protein by remember { mutableStateOf("") }
        var carbs by remember { mutableStateOf("") }
        var fat by remember { mutableStateOf("") }
        val oceanBlueColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF1565C0),
            focusedLabelColor = Color(0xFF1565C0),
            cursorColor = Color(0xFF1565C0)
        )
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Eigenes Lebensmittel") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = oceanBlueColors)
                    OutlinedTextField(value = cal, onValueChange = { cal = it }, label = { Text("Kcal/100g") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(), colors = oceanBlueColors)
                    OutlinedTextField(value = protein, onValueChange = { protein = it }, label = { Text("Protein (g/100g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(), colors = oceanBlueColors)
                    OutlinedTextField(value = carbs, onValueChange = { carbs = it }, label = { Text("Kohlenhydrate (g/100g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(), colors = oceanBlueColors)
                    OutlinedTextField(value = fat, onValueChange = { fat = it }, label = { Text("Fett (g/100g)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth(), colors = oceanBlueColors)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAdd(
                            name,
                            cal.toFloatOrNull() ?: 0f,
                            protein.toFloatOrNull() ?: 0f,
                            carbs.toFloatOrNull() ?: 0f,
                            fat.toFloatOrNull() ?: 0f
                        )
                        showDialog = false
                    },
                    enabled = name.isNotBlank() && cal.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlue)
                ) { Text("Erstellen") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Abbrechen") }
            }
        )
    }
    TextButton(
        onClick = { showDialog = true },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Icon(Icons.Filled.Add, null)
        Spacer(Modifier.width(8.dp))
        Text("Eigenes Lebensmittel erstellen")
    }
}

@Preview(showBackground = true)
@Composable
private fun FoodSearchPreview() {
    JeanFitTheme { FoodSearchScreen(mealType = "breakfast", onBack = {}, onScanBarcode = {}) }
}
