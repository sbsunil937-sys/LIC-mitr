package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "policies",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"]), Index(value = ["policyNumber"], unique = true)]
)
data class PolicyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val policyNumber: String,
    val policyName: String,
    val policyType: String, // Endowment, Term Life, Money Back, Pension, ULIP
    val sumAssured: Double,
    val premiumAmount: Double,
    val frequencyMonths: Int, // 1 (Monthly), 3 (Quarterly), 6 (Half-Yearly), 12 (Yearly)
    val startDate: String, // dd/MM/yyyy
    val nextPremiumDate: String, // dd/MM/yyyy
    val lastPaymentDate: String? = null,
    val nomineeName: String,
    val nomineeRelation: String,
    val agentName: String,
    val agentCode: String,
    val policyDocUri: String? = null,
    val kycDocUri: String? = null,
    val isAutoCalculated: Boolean = true,
    val notes: String = ""
)
