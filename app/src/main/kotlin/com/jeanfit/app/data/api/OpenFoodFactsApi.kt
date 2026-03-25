package com.jeanfit.app.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenFoodFactsApi {
    @GET("api/v2/product/{barcode}?fields=product_name,brands,nutriments,serving_size,image_url")
    suspend fun getProductByBarcode(@Path("barcode") barcode: String): Response<OFFProductResponse>

    @GET("cgi/search.pl")
    suspend fun searchProducts(
        @Query("search_terms") query: String,
        @Query("json") json: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("fields") fields: String = "product_name,brands,nutriments,serving_size,image_url,code"
    ): Response<OFFSearchResponse>
}

@JsonClass(generateAdapter = true)
data class OFFProductResponse(
    @Json(name = "status") val status: Int = 0,
    @Json(name = "product") val product: OFFProduct? = null
)

@JsonClass(generateAdapter = true)
data class OFFSearchResponse(
    @Json(name = "count") val count: Int = 0,
    @Json(name = "products") val products: List<OFFProduct> = emptyList()
)

@JsonClass(generateAdapter = true)
data class OFFProduct(
    @Json(name = "code") val barcode: String? = null,
    @Json(name = "product_name") val productName: String? = null,
    @Json(name = "brands") val brands: String? = null,
    @Json(name = "serving_size") val servingSize: String? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "nutriments") val nutriments: OFFNutriments? = null
)

@JsonClass(generateAdapter = true)
data class OFFNutriments(
    @Json(name = "energy-kcal_100g") val energyKcal100g: Float? = null,
    @Json(name = "energy_100g") val energyKj100g: Float? = null,
    @Json(name = "proteins_100g") val proteins100g: Float? = null,
    @Json(name = "carbohydrates_100g") val carbs100g: Float? = null,
    @Json(name = "fat_100g") val fat100g: Float? = null,
    @Json(name = "fiber_100g") val fiber100g: Float? = null
)
