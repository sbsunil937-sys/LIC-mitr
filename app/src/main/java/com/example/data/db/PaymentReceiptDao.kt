package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.PaymentReceiptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentReceiptDao {
    @Query("SELECT * FROM payment_receipts WHERE policyId = :policyId ORDER BY id DESC")
    fun getReceiptsForPolicy(policyId: Long): Flow<List<PaymentReceiptEntity>>

    @Query("SELECT * FROM payment_receipts ORDER BY id DESC")
    fun getAllReceipts(): Flow<List<PaymentReceiptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: PaymentReceiptEntity): Long

    @Delete
    suspend fun deleteReceipt(receipt: PaymentReceiptEntity)
}
