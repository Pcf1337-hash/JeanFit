package com.jeanfit.app.di

import com.jeanfit.app.BuildConfig
import com.jeanfit.app.data.api.OpenFoodFactsApi
import com.jeanfit.app.data.api.UsdaFoodApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                })
            }
        }
        .build()

    @Provides
    @Singleton
    @Named("OFF")
    fun provideOffRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.OFF_BASE_URL)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    @Named("USDA")
    fun provideUsdaRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.USDA_BASE_URL)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun provideOpenFoodFactsApi(@Named("OFF") retrofit: Retrofit): OpenFoodFactsApi =
        retrofit.create(OpenFoodFactsApi::class.java)

    @Provides
    @Singleton
    fun provideUsdaFoodApi(@Named("USDA") retrofit: Retrofit): UsdaFoodApi =
        retrofit.create(UsdaFoodApi::class.java)
}
