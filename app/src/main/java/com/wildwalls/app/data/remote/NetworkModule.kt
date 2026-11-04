package com.wildwalls.app.data.remote

import android.content.Context
import com.wildwalls.app.BuildConfig
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Two clients share the same tuning but different responsibilities:
 *  - [apiClient] talks to the MediaWiki API and injects the Commons query contract.
 *  - [imageClient] fetches upload.wikimedia.org files and only sets the identifying User-Agent.
 */
object NetworkModule {

    private const val BASE_URL = "https://commons.wikimedia.org/"
    private const val CACHE_BYTES = 30L * 1024 * 1024

    fun apiClient(context: Context, thumbWidth: Int): OkHttpClient =
        base(context, "commons_http")
            .addInterceptor(CommonsParamsInterceptor(thumbWidth))
            .addInterceptor(RetryInterceptor(maxAttempts = 3))
            .build()

    fun imageClient(context: Context): OkHttpClient =
        base(context, "image_http")
            .addInterceptor(RetryInterceptor(maxAttempts = 3))
            .build()

    private fun base(context: Context, cacheName: String): OkHttpClient.Builder {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .cache(Cache(File(context.cacheDir, cacheName), CACHE_BYTES))
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .followRedirects(true)
    }

    private val userAgentInterceptor = Interceptor { chain ->
        val request = chain.request().newBuilder()
            .header("User-Agent", CommonsParamsInterceptor.USER_AGENT)
            .build()
        chain.proceed(request)
    }

    fun retrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    fun api(client: OkHttpClient): WikimediaApi = retrofit(client).create(WikimediaApi::class.java)
}
