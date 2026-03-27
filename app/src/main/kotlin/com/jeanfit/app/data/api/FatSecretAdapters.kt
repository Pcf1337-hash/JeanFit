package com.jeanfit.app.data.api

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 * FatSecret gibt "servings" → "serving" als JSON-Objekt ODER als JSON-Array zurück.
 * Dieser Adapter normalisiert beides zu List<FatSecretServing>.
 *
 * LESSON-016: Immer diesen Adapter nutzen, nie direkt auf FatSecretServings casten.
 */
class FatSecretServingsAdapter {

    @FromJson
    fun fromJson(reader: JsonReader): FatSecretServings {
        var servingList = emptyList<FatSecretServing>()
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "serving" -> servingList = when (reader.peek()) {
                    JsonReader.Token.BEGIN_ARRAY -> readServingList(reader)
                    JsonReader.Token.BEGIN_OBJECT -> listOf(readServing(reader))
                    else -> { reader.skipValue(); emptyList() }
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return FatSecretServings(servingList)
    }

    @ToJson
    fun toJson(@Suppress("UNUSED_PARAMETER") writer: JsonWriter, @Suppress("UNUSED_PARAMETER") value: FatSecretServings) {
        writer.beginObject(); writer.endObject()
    }

    private fun readServingList(reader: JsonReader): List<FatSecretServing> {
        val list = mutableListOf<FatSecretServing>()
        reader.beginArray()
        while (reader.hasNext()) list.add(readServing(reader))
        reader.endArray()
        return list
    }

    private fun readServing(reader: JsonReader): FatSecretServing {
        var servingId = "0"
        var description = "100g"
        var amountG: String? = null
        var calories = "0"
        var protein = "0"
        var carbs = "0"
        var fat = "0"
        var fiber: String? = null

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "serving_id"              -> servingId    = reader.nextString()
                "serving_description"     -> description  = reader.nextString()
                "metric_serving_amount"   -> amountG      = reader.nextString()
                "calories"                -> calories     = reader.nextString()
                "protein"                 -> protein      = reader.nextString()
                "carbohydrate"            -> carbs        = reader.nextString()
                "fat"                     -> fat          = reader.nextString()
                "fiber"                   -> fiber        = reader.nextString()
                else                      -> reader.skipValue()
            }
        }
        reader.endObject()
        return FatSecretServing(servingId, description, amountG, calories, protein, carbs, fat, fiber)
    }
}

/**
 * FatSecret gibt "foods" → "food" als JSON-Objekt (1 Treffer) ODER als Array (>1) zurück.
 */
class FatSecretFoodsWrapperAdapter {

    @FromJson
    fun fromJson(reader: JsonReader): FatSecretFoodsWrapper {
        var foodList = emptyList<FatSecretFoodSummary>()
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "food" -> foodList = when (reader.peek()) {
                    JsonReader.Token.BEGIN_ARRAY -> readFoodList(reader)
                    JsonReader.Token.BEGIN_OBJECT -> listOf(readFoodSummary(reader))
                    else -> { reader.skipValue(); emptyList() }
                }
                else -> reader.skipValue()
            }
        }
        reader.endObject()
        return FatSecretFoodsWrapper(foodList)
    }

    @ToJson
    fun toJson(@Suppress("UNUSED_PARAMETER") writer: JsonWriter, @Suppress("UNUSED_PARAMETER") value: FatSecretFoodsWrapper) {
        writer.beginObject(); writer.endObject()
    }

    private fun readFoodList(reader: JsonReader): List<FatSecretFoodSummary> {
        val list = mutableListOf<FatSecretFoodSummary>()
        reader.beginArray()
        while (reader.hasNext()) list.add(readFoodSummary(reader))
        reader.endArray()
        return list
    }

    private fun readFoodSummary(reader: JsonReader): FatSecretFoodSummary {
        var foodId = ""
        var name = ""
        var brand: String? = null
        var desc = ""

        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.nextName()) {
                "food_id"          -> foodId = reader.nextString()
                "food_name"        -> name   = reader.nextString()
                "brand_name"       -> brand  = reader.nextString()
                "food_description" -> desc   = reader.nextString()
                else               -> reader.skipValue()
            }
        }
        reader.endObject()
        return FatSecretFoodSummary(foodId, name, brand, desc)
    }
}
