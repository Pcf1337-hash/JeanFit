package com.jeanfit.app.data.repository

import com.jeanfit.app.data.api.FatSecretApi
import com.jeanfit.app.data.api.FatSecretFood
import com.jeanfit.app.data.api.FatSecretFoodSummary
import com.jeanfit.app.data.api.OFFProduct
import com.jeanfit.app.data.api.OpenFoodFactsApi
import com.jeanfit.app.data.api.UsdaFoodApi
import com.jeanfit.app.data.db.dao.FoodDao
import com.jeanfit.app.data.db.entities.FoodItem
import com.jeanfit.app.data.db.entities.FoodLogEntry
import com.jeanfit.app.domain.usecase.CalcColorCategoryUseCase
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepository @Inject constructor(
    private val dao: FoodDao,
    private val offApi: OpenFoodFactsApi,
    private val usdaApi: UsdaFoodApi,
    private val fatSecretApi: FatSecretApi,
    private val calcColorCategory: CalcColorCategoryUseCase
) {
    fun searchLocal(query: String): Flow<List<FoodItem>> = dao.searchFoodItems(query)

    /**
     * Waterfall: OpenFoodFacts → FatSecret.
     * Ergebnisse aus beiden Quellen werden zusammengeführt (dedupliziert per foodId).
     */
    suspend fun searchRemote(query: String): List<FoodItem> {
        val offItems = try {
            val response = offApi.searchProducts(query)
            if (response.isSuccessful)
                response.body()?.products?.mapNotNull { it.toFoodItem(calcColorCategory) } ?: emptyList()
            else emptyList()
        } catch (_: Exception) { emptyList() }

        val fatItems = try {
            val response = fatSecretApi.searchFoods(query = query)
            if (response.isSuccessful)
                response.body()?.foods?.food?.mapNotNull {
                    it.toFoodItem(calcColorCategory)
                } ?: emptyList()
            else emptyList()
        } catch (_: Exception) { emptyList() }

        val merged = (offItems + fatItems).distinctBy { it.foodId }
        if (merged.isNotEmpty()) dao.insertFoodItems(merged)
        return merged
    }

    /**
     * Barcode-Lookup Waterfall: DB → OpenFoodFacts → FatSecret.
     */
    suspend fun getByBarcode(barcode: String): FoodItem? {
        dao.getFoodItemByBarcode(barcode)?.let { return it }

        // 1. OpenFoodFacts
        try {
            val response = offApi.getProductByBarcode(barcode)
            if (response.isSuccessful && response.body()?.status == 1) {
                val item = response.body()?.product?.toFoodItem(calcColorCategory)
                item?.let { dao.insertFoodItem(it) }
                if (item != null) return item
            }
        } catch (_: Exception) { }

        // 2. FatSecret Fallback
        return try {
            val barcodeResp = fatSecretApi.searchByBarcode(barcode = barcode)
            val foodId = barcodeResp.body()?.foodId?.value ?: return null
            val detailResp = fatSecretApi.getFoodById(foodId = foodId)
            val item = detailResp.body()?.food?.toFoodItem(calcColorCategory, barcode)
            item?.let { dao.insertFoodItem(it) }
            item
        } catch (_: Exception) { null }
    }

    fun getRecentItems(): Flow<List<FoodItem>> = dao.getRecentFoodItems()

    suspend fun insertFoodItem(item: FoodItem) = dao.insertFoodItem(item)

    suspend fun logEntry(entry: FoodLogEntry): Long = dao.insertLogEntry(entry)

    suspend fun deleteLogEntry(id: Long) = dao.deleteLogEntry(id)

    suspend fun updateLogEntry(id: Long, multiplier: Float, sizeG: Float, calories: Float, protein: Float, carbs: Float, fat: Float) =
        dao.updateLogEntry(id, multiplier, sizeG, calories, protein, carbs, fat)

    fun getEntriesForDay(dayEpoch: Long): Flow<List<FoodLogEntry>> = dao.getEntriesForDay(dayEpoch)

    fun getCaloriesForDay(dayEpoch: Long): Flow<Float?> = dao.getCaloriesForDay(dayEpoch)
}

// ─── OpenFoodFacts Mapper ─────────────────────────────────────────────────────

private fun OFFProduct.toFoodItem(calcColorCategory: CalcColorCategoryUseCase): FoodItem? {
    val name = productName?.takeIf { it.isNotBlank() } ?: return null
    val cal = nutriments?.energyKcal100g
        ?: (nutriments?.energyKj100g?.let { it / 4.184f })
        ?: 0f
    return FoodItem(
        foodId = barcode ?: UUID.randomUUID().toString(),
        name = name,
        brand = brands?.split(",")?.firstOrNull()?.trim(),
        barcode = barcode,
        caloriesPer100g = cal,
        proteinPer100g = nutriments?.proteins100g ?: 0f,
        carbsPer100g = nutriments?.carbs100g ?: 0f,
        fatPer100g = nutriments?.fat100g ?: 0f,
        fiberPer100g = nutriments?.fiber100g,
        defaultServingSizeG = parseServingSize(servingSize),
        colorCategory = calcColorCategory.calculate(cal),
        calorieDensity = cal / 100f,
        source = "openfoodfacts",
        unit = if (isLiquid(servingSize, name)) "ml" else "g"
    )
}

// ─── FatSecret Mappers ────────────────────────────────────────────────────────

/**
 * Mappt FatSecretFoodSummary (Suchergebnis) auf FoodItem.
 * Die Nährwerte werden aus dem description-String geparst:
 * "Per 100g - Calories: 89kcal | Fat: 0.33g | Carbs: 22.84g | Protein: 1.09g"
 */
private fun FatSecretFoodSummary.toFoodItem(calcColorCategory: CalcColorCategoryUseCase): FoodItem? {
    if (foodId.isBlank() || name.isBlank()) return null
    val cal     = Regex("Calories:\\s*(\\d+(?:\\.\\d+)?)").find(description)?.groupValues?.get(1)?.toFloatOrNull() ?: return null
    val fat     = Regex("Fat:\\s*(\\d+(?:\\.\\d+)?)").find(description)?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
    val carbs   = Regex("Carbs:\\s*(\\d+(?:\\.\\d+)?)").find(description)?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
    val protein = Regex("Protein:\\s*(\\d+(?:\\.\\d+)?)").find(description)?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
    return FoodItem(
        foodId            = "fs_$foodId",
        name              = name,
        brand             = brand,
        barcode           = null,
        caloriesPer100g   = cal,
        proteinPer100g    = protein,
        carbsPer100g      = carbs,
        fatPer100g        = fat,
        fiberPer100g      = null,
        defaultServingSizeG = 100f,
        colorCategory     = calcColorCategory.calculate(cal),
        calorieDensity    = cal / 100f,
        source            = "fatsecret",
        unit              = "g"
    )
}

/**
 * Mappt FatSecretFood (Detail) auf FoodItem.
 * Normalisiert alle Nährwerte auf 100g Basis.
 */
private fun FatSecretFood.toFoodItem(
    calcColorCategory: CalcColorCategoryUseCase,
    barcode: String? = null
): FoodItem? {
    if (foodId.isBlank() || name.isBlank()) return null
    // Bevorzuge "Per 100g" Serving, sonst die erste verfügbare
    val serving = servings.servingList
        .firstOrNull { "100" in it.description && ("g" in it.description || "ml" in it.description) }
        ?: servings.servingList.firstOrNull()
        ?: return null

    val amountG = serving.amountG?.toFloatOrNull()?.takeIf { it > 0 } ?: 100f
    val factor  = 100f / amountG

    val cal     = (serving.calories.toFloatOrNull() ?: 0f) * factor
    val protein = (serving.protein.toFloatOrNull()  ?: 0f) * factor
    val carbs   = (serving.carbs.toFloatOrNull()    ?: 0f) * factor
    val fat     = (serving.fat.toFloatOrNull()      ?: 0f) * factor
    val fiber   = serving.fiber?.toFloatOrNull()?.times(factor)

    val isLiquidFood = type.contains("drink", ignoreCase = true)
            || name.contains("milk", ignoreCase = true)
            || name.contains("juice", ignoreCase = true)
            || name.contains("saft", ignoreCase = true)

    return FoodItem(
        foodId              = "fs_$foodId",
        name                = name,
        brand               = brand,
        barcode             = barcode,
        caloriesPer100g     = cal,
        proteinPer100g      = protein,
        carbsPer100g        = carbs,
        fatPer100g          = fat,
        fiberPer100g        = fiber,
        defaultServingSizeG = amountG.coerceIn(5f, 1000f),
        colorCategory       = calcColorCategory.calculate(cal),
        calorieDensity      = cal / 100f,
        source              = "fatsecret",
        unit                = if (isLiquidFood) "ml" else "g"
    )
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun isLiquid(servingSize: String?, name: String): Boolean {
    val sLower = servingSize?.lowercase() ?: ""
    val nLower = name.lowercase()
    if (sLower.contains("ml") || sLower.contains(" cl") || Regex("\\d+\\s*l\\b").containsMatchIn(sLower)) return true
    val drinkKeywords = listOf("wasser", "water", "saft", "juice", "milch", "milk", "tee", "tea",
        "kaffee", "coffee", "bier", "beer", "wein", "wine", "limonade", "limo", "cola",
        "energy drink", "smoothie", "getränk", "drink", "soda", "brause", "nektar", "nectar")
    return drinkKeywords.any { nLower.contains(it) }
}

private fun parseServingSize(servingSize: String?): Float {
    if (servingSize == null) return 100f
    val number = Regex("(\\d+(?:\\.\\d+)?)").find(servingSize)?.groupValues?.get(1)?.toFloatOrNull()
    return number?.takeIf { it in 5f..1000f } ?: 100f
}
