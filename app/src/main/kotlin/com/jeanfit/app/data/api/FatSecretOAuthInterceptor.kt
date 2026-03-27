package com.jeanfit.app.data.api

import android.util.Base64
import okhttp3.Interceptor
import okhttp3.Response
import java.net.URLEncoder
import java.util.TreeMap
import java.util.UUID
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class FatSecretOAuthInterceptor(
    private val consumerKey: String,
    private val consumerSecret: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url

        val timestamp = (System.currentTimeMillis() / 1000).toString()
        val nonce = UUID.randomUUID().toString().replace("-", "")

        // Alle OAuth + Request-Parameter in sortierter Map
        val params = TreeMap<String, String>().apply {
            put("oauth_consumer_key", consumerKey)
            put("oauth_nonce", nonce)
            put("oauth_signature_method", "HMAC-SHA1")
            put("oauth_timestamp", timestamp)
            put("oauth_version", "1.0")
            for (i in 0 until url.querySize) {
                put(url.queryParameterName(i), url.queryParameterValue(i) ?: "")
            }
        }

        // Basis-String
        val paramString = params.entries.joinToString("&") { "${encode(it.key)}=${encode(it.value)}" }
        val baseUrl = url.toString().substringBefore("?")
        val baseString = "GET&${encode(baseUrl)}&${encode(paramString)}"

        // HMAC-SHA1 Signatur
        val signingKey = "${encode(consumerSecret)}&"
        val mac = Mac.getInstance("HmacSHA1").apply {
            init(SecretKeySpec(signingKey.toByteArray(Charsets.UTF_8), "HmacSHA1"))
        }
        val signature = Base64.encodeToString(
            mac.doFinal(baseString.toByteArray(Charsets.UTF_8)),
            Base64.NO_WRAP
        )

        val signedUrl = url.newBuilder()
            .addQueryParameter("oauth_consumer_key", consumerKey)
            .addQueryParameter("oauth_nonce", nonce)
            .addQueryParameter("oauth_signature_method", "HMAC-SHA1")
            .addQueryParameter("oauth_timestamp", timestamp)
            .addQueryParameter("oauth_version", "1.0")
            .addQueryParameter("oauth_signature", signature)
            .build()

        return chain.proceed(original.newBuilder().url(signedUrl).build())
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, "UTF-8")
            .replace("+", "%20")
            .replace("*", "%2A")
            .replace("%7E", "~")
}
