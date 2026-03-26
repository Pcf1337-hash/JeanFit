package com.jeanfit.app.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface GithubApi {
    @GET
    suspend fun getLatestRelease(@Url url: String): Response<GithubRelease>
}

@JsonClass(generateAdapter = true)
data class GithubRelease(
    @Json(name = "tag_name")     val tagName: String,
    @Json(name = "name")         val name: String,
    @Json(name = "body")         val body: String?,
    @Json(name = "published_at") val publishedAt: String,
    @Json(name = "assets")       val assets: List<GithubAsset>
)

@JsonClass(generateAdapter = true)
data class GithubAsset(
    @Json(name = "name")                  val name: String,
    @Json(name = "browser_download_url")  val browserDownloadUrl: String,
    @Json(name = "size")                  val size: Long
)
