package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val dob: String,
    val mobileNumber: String,
    val email: String,
    val address: String,
    val profilePhotoUri: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
)
