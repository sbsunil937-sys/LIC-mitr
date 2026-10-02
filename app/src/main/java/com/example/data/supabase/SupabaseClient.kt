package com.example.data.supabase

import com.example.data.supabase.model.SupabasePaymentReceiptDto
import com.example.data.supabase.model.SupabasePolicyDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class SupabaseClient {

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val key = SupabaseConfig.apiKey
        val requestBuilder = original.newBuilder()
            .header("apikey", key)
            .header("Authorization", "Bearer $key")
            .header("Content-Type", "application/json")
            .method(original.method, original.body)
        chain.proceed(requestBuilder.build())
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(SupabaseConfig.baseUrl.trimEnd('/') + "/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: SupabaseService = retrofit.create(SupabaseService::class.java)

    /**
     * Attempts to store policy in Supabase.
     * Tries 'policies' table, then falls back to 'policy_holders' table.
     */
    suspend fun syncPolicy(policy: SupabasePolicyDto): Result<String> {
        return try {
            val response = api.insertPolicy(policy)
            if (response.isSuccessful) {
                Result.success("Stored in Supabase (policies table)")
            } else if (response.code() == 404) {
                // Try fallback table name 'policy_holders'
                val fallbackResponse = api.insertPolicyHolder(policy)
                if (fallbackResponse.isSuccessful) {
                    Result.success("Stored in Supabase (policy_holders table)")
                } else {
                    Result.failure(Exception("Supabase HTTP ${fallbackResponse.code()}: ${fallbackResponse.errorBody()?.string()}"))
                }
            } else {
                Result.failure(Exception("Supabase HTTP ${response.code()}: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncReceipt(receipt: SupabasePaymentReceiptDto): Result<String> {
        return try {
            val response = api.insertReceipt(receipt)
            if (response.isSuccessful) {
                Result.success("Receipt synced with Supabase")
            } else {
                Result.failure(Exception("Supabase HTTP ${response.code()}: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkConnection(): Boolean {
        return try {
            val response = api.pingApi()
            response.isSuccessful || response.code() == 200 || response.code() == 404
        } catch (_: Exception) {
            false
        }
    }
}
