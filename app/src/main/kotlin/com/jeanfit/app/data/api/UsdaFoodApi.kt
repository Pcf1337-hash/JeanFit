package com.jeanfit.app.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface UsdaFoodApi {
    @GET("v1/foods/search")
    suspend fun searchFoods(
        @Query("query") query: String,
        @Query("api_key") apiKey: String,
        @Query("pageSize") pageSize: Int = 20,
        @Query("dataType") dataType: String = "Foundation,SR Legacy"
    ): Response<UsdaSearchResponse>
}

@JsonClass(generateAdapter = true)
data class UsdaSearchResponse(
    @Json(name = "totalHits") val totalHits: Int = 0,
    @Json(name = "foods") val foods: List<UsdaFood> = emptyList()
)

@JsonClass(generateAdapter = true)
data class UsdaFood(
    @Json(name = "fdcId") val fdcId: Int = 0,
    @Json(name = "description") val description: String = "",
    @Json(name = "brandOwner") val brandOwner: String? = null,
    @Json(name = "servingSize") val servingSize: Float? = null,
    @Json(name = "servingSizeUnit") val servingSizeUnit: String? = null,
    @Json(name = "foodNutrients") val foodNutrients: List<UsdaNutrient> = emptyList()
)

@JsonClass(generateAdapter = true)
data class UsdaNutrient(
    @Json(name = "nutrientId") val nutrientId: Int = 0,
    @Json(name = "nutrientName") val nutrientName: String = "",
    @Json(name = "value") val value: Float? = null,
    @Json(name = "unitName") val unitName: String = ""
)
