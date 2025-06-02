package com.example.soilscout.ui.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.soilscout.MyApplication
import com.example.soilscout.model.Notification
import kotlinx.coroutines.launch

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {

    val allNotifications: LiveData<List<Notification>> =
        MyApplication.notificationDao.getAllNotifications().asLiveData()

    fun clearAllNotifications() {
        viewModelScope.launch {
            MyApplication.notificationDao.deleteAllNotifications()
        }
    }
}