package com.jeanfit.app.data.api

import com.squareup.moshi.Json

// --- Barcode Response ---

data class FatSecretBarcodeResponse(
    @Json(name = "food_id") val foodId: FatSecretFoodIdWrapper? = null
)

data class FatSecretFoodIdWrapper(
    @Json(name = "value") val value: String? = null
)

// --- Food Detail Response ---

data class FatSecretFoodResponse(
    @Json(name = "food") val food: FatSecretFood? = null
)

data class FatSecretFood(
    @Json(name = "food_id")    val foodId: String = "",
    @Json(name = "food_name")  val name: String = "",
    @Json(name = "food_type")  val type: String = "Generic",
    @Json(name = "brand_name") val brand: String? = null,
    @Json(name = "servings")   val servings: FatSecretServings = FatSecretServings(emptyList())
)

/**
 * Wrapper für "servings" → "serving" Feld.
 * FatSecret gibt "serving" als Objekt ODER als Array zurück — der
 * [FatSecretServingsAdapter] normalisiert das zu einer Liste.
 */
data class FatSecretServings(
    val servingList: List<FatSecretServing> = emptyList()
)

data class FatSecretServing(
    @Json(name = "serving_id")              val servingId: String = "0",
    @Json(name = "serving_description")     val description: String = "100g",
    @Json(name = "metric_serving_amount")   val amountG: String? = null,
    @Json(name = "calories")                val calories: String = "0",
    @Json(name = "protein")                 val protein: String = "0",
    @Json(name = "carbohydrate")            val carbs: String = "0",
    @Json(name = "fat")                     val fat: String = "0",
    @Json(name = "fiber")                   val fiber: String? = null
)

// --- Search Response ---

data class FatSecretSearchResponse(
    @Json(name = "foods") val foods: FatSecretFoodsWrapper? = null
)

/**
 * Wrapper für "foods" → "food" Feld.
 * Analog zu serving kann "food" ein Objekt oder eine Liste sein.
 * Der [FatSecretFoodsWrapperAdapter] normalisiert das.
 */
data class FatSecretFoodsWrapper(
    val food: List<FatSecretFoodSummary> = emptyList()
)

data class FatSecretFoodSummary(
    @Json(name = "food_id")          val foodId: String = "",
    @Json(name = "food_name")        val name: String = "",
    @Json(name = "brand_name")       val brand: String? = null,
    @Json(name = "food_description") val description: String = ""
)
