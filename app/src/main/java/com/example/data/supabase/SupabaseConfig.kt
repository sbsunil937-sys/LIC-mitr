package com.example.data.supabase

import com.example.BuildConfig

object SupabaseConfig {
    // Project ID provided: fldgbvzimoxfnjavzdpg
    const val DEFAULT_PROJECT_ID = "fldgbvzimoxfnjavzdpg"
    const val DEFAULT_BASE_URL = "https://fldgbvzimoxfnjavzdpg.supabase.co"
    const val DEFAULT_API_KEY = "sb_publishable_VBFsjwIkbnw9dmVJA7T3kQ_U6A-Uhs"

    val projectId: String
        get() = try {
            val fromBuildConfig = BuildConfig.SUPABASE_PROJECT_ID
            if (!fromBuildConfig.isNullOrBlank()) fromBuildConfig else DEFAULT_PROJECT_ID
        } catch (_: Exception) {
            DEFAULT_PROJECT_ID
        }

    val baseUrl: String
        get() = try {
            val fromBuildConfig = BuildConfig.SUPABASE_URL
            if (!fromBuildConfig.isNullOrBlank()) fromBuildConfig else DEFAULT_BASE_URL
        } catch (_: Exception) {
            DEFAULT_BASE_URL
        }

    val apiKey: String
        get() = try {
            val fromBuildConfig = BuildConfig.SUPABASE_KEY
            if (!fromBuildConfig.isNullOrBlank()) fromBuildConfig else DEFAULT_API_KEY
        } catch (_: Exception) {
            DEFAULT_API_KEY
        }
}
