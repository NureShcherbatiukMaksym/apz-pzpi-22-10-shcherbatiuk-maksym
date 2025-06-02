
package com.example.soilscout.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.example.soilscout.model.Notification

@Dao
interface NotificationDao {
    @Insert
    suspend fun insertNotification(notification: Notification)

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<Notification>>

    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()
}