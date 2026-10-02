package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payment_receipts",
    foreignKeys = [
        ForeignKey(
            entity = PolicyEntity::class,
            parentColumns = ["id"],
            childColumns = ["policyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["policyId"])]
)
data class PaymentReceiptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val policyId: Long,
    val paymentDate: String, // dd/MM/yyyy
    val amountPaid: Double,
    val transactionRef: String,
    val paymentMode: String, // UPI / GPay, Net Banking, Debit Card, Cheque, Cash
    val receiptImageUri: String? = null,
    val periodCovered: String = "",
    val recordedAtMillis: Long = System.currentTimeMillis()
)
