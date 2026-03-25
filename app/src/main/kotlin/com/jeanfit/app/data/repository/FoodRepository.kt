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

    fun getEntriesForDay(dayEpoch: Long): Flow<List<FoodLogEntry>> = dao.getEntriesForDay(dayEpoch)

    fun getCaloriesForDay(dayEpoch: Long): Flow<Float?> = dao.getCaloriesForDay(dayEpoch)
}

private fun OFFProduct.toFoodItem(calcColorCategory: CalcColorCategoryUseCase): FoodItem? {
    val name = productName?.takeIf { it.isNotBlank() } ?: return null
    val cal = nutriments?.energyKcal100g ?: (nutriments?.energyKj100g?.let { it / 4.184f }) ?: return null
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
        defaultServingSizeG = 100f,
        colorCategory = calcColorCategory.calculate(cal),
        calorieDensity = cal / 100f,
        source = "openfoodfacts"
    )
}
