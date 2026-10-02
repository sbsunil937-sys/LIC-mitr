package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.PolicyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PolicyDao {
    @Query("SELECT * FROM policies ORDER BY id DESC")
    fun getAllPolicies(): Flow<List<PolicyEntity>>

    @Query("SELECT COUNT(*) FROM policies")
    suspend fun getPolicyCount(): Int

    @Query("SELECT * FROM policies WHERE userId = :userId ORDER BY id DESC")
    fun getPoliciesByUserId(userId: Long): Flow<List<PolicyEntity>>

    @Query("SELECT * FROM policies WHERE id = :id LIMIT 1")
    suspend fun getPolicyById(id: Long): PolicyEntity?

    @Query("SELECT * FROM policies WHERE id = :id LIMIT 1")
    fun getPolicyFlow(id: Long): Flow<PolicyEntity?>

    @Query("SELECT * FROM policies WHERE policyNumber LIKE '%' || :query || '%' OR policyName LIKE '%' || :query || '%' ORDER BY id DESC")
    fun searchPolicies(query: String): Flow<List<PolicyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPolicy(policy: PolicyEntity): Long

    @Update
    suspend fun updatePolicy(policy: PolicyEntity)

    @Delete
    suspend fun deletePolicy(policy: PolicyEntity)

    @Query("DELETE FROM policies WHERE id = :id")
    suspend fun deletePolicyById(id: Long)
}
