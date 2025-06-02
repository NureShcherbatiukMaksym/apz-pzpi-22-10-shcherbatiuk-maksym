package com.example.soilscout.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val message: String,
    val pointInfo: String,
    val timestamp: Long,
    val status: String,
    val fieldId: Int,
    val pointId: Int
)