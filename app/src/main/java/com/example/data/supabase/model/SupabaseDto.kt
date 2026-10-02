package com.example.data.supabase.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SupabasePolicyDto(
    @Json(name = "policy_number") val policyNumber: String,
    @Json(name = "customer_name") val customerName: String,
    @Json(name = "customer_dob") val customerDob: String,
    @Json(name = "mobile_number") val mobileNumber: String,
    @Json(name = "email") val email: String = "",
    @Json(name = "address") val address: String = "",
    @Json(name = "policy_name") val policyName: String,
    @Json(name = "policy_type") val policyType: String,
    @Json(name = "sum_assured") val sumAssured: Double,
    @Json(name = "premium_amount") val premiumAmount: Double,
    @Json(name = "frequency_months") val frequencyMonths: Int,
    @Json(name = "start_date") val startDate: String,
    @Json(name = "next_premium_date") val nextPremiumDate: String,
    @Json(name = "last_payment_date") val lastPaymentDate: String? = null,
    @Json(name = "nominee_name") val nomineeName: String = "",
    @Json(name = "nominee_relation") val nomineeRelation: String = "",
    @Json(name = "agent_name") val agentName: String = "",
    @Json(name = "agent_code") val agentCode: String = "",
    @Json(name = "status") val status: String = "ACTIVE",
    @Json(name = "notes") val notes: String = ""
)

@JsonClass(generateAdapter = true)
data class SupabasePaymentReceiptDto(
    @Json(name = "policy_number") val policyNumber: String,
    @Json(name = "payment_date") val paymentDate: String,
    @Json(name = "amount_paid") val amountPaid: Double,
    @Json(name = "payment_mode") val paymentMode: String,
    @Json(name = "transaction_ref") val transactionRef: String,
    @Json(name = "period_covered") val periodCovered: String = ""
)

@JsonClass(generateAdapter = true)
data class SupabaseSyncResult(
    val success: Boolean,
    val message: String,
    val recordsSynced: Int = 0
)
