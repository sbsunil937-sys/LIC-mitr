package com.example.data.supabase

import com.example.data.supabase.model.SupabasePaymentReceiptDto
import com.example.data.supabase.model.SupabasePolicyDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseService {

    /**
     * Inserts or upserts a policy into the 'policies' table in Supabase.
     */
    @Headers("Prefer: return=representation,resolution=merge-duplicates")
    @POST("rest/v1/policies")
    suspend fun insertPolicy(
        @Body policy: SupabasePolicyDto
    ): Response<ResponseBody>

    /**
     * Alternative table fallback if table created as 'policy_holders'.
     */
    @Headers("Prefer: return=representation,resolution=merge-duplicates")
    @POST("rest/v1/policy_holders")
    suspend fun insertPolicyHolder(
        @Body policy: SupabasePolicyDto
    ): Response<ResponseBody>

    /**
     * Fetch all policies stored in Supabase.
     */
    @GET("rest/v1/policies")
    suspend fun getPolicies(
        @Query("select") select: String = "*"
    ): Response<List<SupabasePolicyDto>>

    /**
     * Inserts a payment receipt into 'payment_receipts' table in Supabase.
     */
    @Headers("Prefer: return=representation")
    @POST("rest/v1/payment_receipts")
    suspend fun insertReceipt(
        @Body receipt: SupabasePaymentReceiptDto
    ): Response<ResponseBody>

    /**
     * Health check endpoint to test connection to Supabase instance.
     */
    @GET("rest/v1/")
    suspend fun pingApi(): Response<ResponseBody>
}
