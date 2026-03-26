package com.jeanfit.app.data.repository

import com.jeanfit.app.data.api.OFFProduct
import com.jeanfit.app.data.api.OpenFoodFactsApi
import com.jeanfit.app.data.api.UsdaFoodApi
import com.jeanfit.app.data.db.dao.FoodDao
import com.jeanfit.app.data.db.entities.FoodItem
import com.jeanfit.app.data.db.entities.FoodLogEntry
import com.jeanfit.app.domain.usecase.CalcColorCategoryUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepository @Inject constructor(
    private val dao: FoodDao,
    private val offApi: OpenFoodFactsApi,
    private val usdaApi: UsdaFoodApi,
    private val calcColorCategory: CalcColorCategoryUseCase
) {
    fun searchLocal(query: String): Flow<List<FoodItem>> = dao.searchFoodItems(query)

    suspend fun searchRemote(query: String): List<FoodItem> {
        return try {
            val response = offApi.searchProducts(query)
            if (response.isSuccessful) {
                val items = response.body()?.products
                    ?.mapNotNull { it.toFoodItem(calcColorCategory) } ?: emptyList()
                dao.insertFoodItems(items)
                items
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getByBarcode(barcode: String): FoodItem? {
        val local = dao.getFoodItemByBarcode(barcode)
        if (local != null) return local
        return try {
            val response = offApi.getProductByBarcode(barcode)
            if (response.isSuccessful && response.body()?.status == 1) {
                val item = response.body()?.product?.toFoodItem(calcColorCategory)
                item?.let { dao.insertFoodItem(it) }
                item
            } else null
        } catch (e: Exception) {
            null
        }
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

private fun OFFProduct.toFoodItem(calcColorCategory: CalcColorCategoryUseCase): FoodItem? {
    val name = productName?.takeIf { it.isNotBlank() } ?: return null
    // Accept products with unknown calories (shown as 0) — better than showing no results
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

/** Prüft anhand des Serving-Size-Strings und Namens ob es sich um ein Getränk handelt */
private fun isLiquid(servingSize: String?, name: String): Boolean {
    val sLower = servingSize?.lowercase() ?: ""
    val nLower = name.lowercase()
    // Explizite ml/cl/l Einheit im Serving Size
    if (sLower.contains("ml") || sLower.contains(" cl") || Regex("\\d+\\s*l\\b").containsMatchIn(sLower)) return true
    // Typische Getränke-Keywords im Produktnamen
    val drinkKeywords = listOf("wasser", "water", "saft", "juice", "milch", "milk", "tee", "tea",
        "kaffee", "coffee", "bier", "beer", "wein", "wine", "limonade", "limo", "cola",
        "energy drink", "smoothie", "getränk", "drink", "soda", "brause", "nektar", "nectar")
    return drinkKeywords.any { nLower.contains(it) }
}

/** Extrahiert Portionsgröße aus String wie "250ml" oder "100 g" */
private fun parseServingSize(servingSize: String?): Float {
    if (servingSize == null) return 100f
    val number = Regex("(\\d+(?:\\.\\d+)?)").find(servingSize)?.groupValues?.get(1)?.toFloatOrNull()
    return number?.takeIf { it in 5f..1000f } ?: 100f
}
