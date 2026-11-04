package com.wildwalls.app.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Network retry with exponential backoff. Commons rate limits (HTTP 429) and transient 5xx
 * responses are retried; 4xx client errors are returned immediately.
 */
class RetryInterceptor(private val maxAttempts: Int = 3) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var lastError: IOException? = null
        while (attempt < maxAttempts) {
            try {
                val response = chain.proceed(chain.request())
                val retryable = response.code == 429 || response.code in 500..599
                if (!retryable || attempt == maxAttempts - 1) return response
                response.close()
            } catch (e: IOException) {
                lastError = e
                if (attempt == maxAttempts - 1) throw e
            }
            attempt++
            Thread.sleep(600L * attempt * attempt)
        }
        throw lastError ?: IOException("Request failed after $maxAttempts attempts")
    }
}
