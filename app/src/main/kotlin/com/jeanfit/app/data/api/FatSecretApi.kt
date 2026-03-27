package com.jeanfit.app.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface FatSecretApi {

    /** Barcode → FatSecret Food ID */
    @GET("rest/server.api")
    suspend fun searchByBarcode(
        @Query("method")  method:  String = "food.find_id_for_barcode",
        @Query("barcode") barcode: String,
        @Query("format")  format:  String = "json"
    ): Response<FatSecretBarcodeResponse>

    /** Food ID → vollständige Nährwerte */
    @GET("rest/server.api")
    suspend fun getFoodById(
        @Query("method")  method: String = "food.get.v4",
        @Query("food_id") foodId: String,
        @Query("format")  format: String = "json"
    ): Response<FatSecretFoodResponse>

    /** Textsuche */
    @GET("rest/server.api")
    suspend fun searchFoods(
        @Query("method")            method:     String = "foods.search",
        @Query("search_expression") query:      String,
        @Query("max_results")       maxResults: Int    = 20,
        @Query("format")            format:     String = "json"
    ): Response<FatSecretSearchResponse>
}
// BaseUrl: "https://platform.fatsecret.com/"
