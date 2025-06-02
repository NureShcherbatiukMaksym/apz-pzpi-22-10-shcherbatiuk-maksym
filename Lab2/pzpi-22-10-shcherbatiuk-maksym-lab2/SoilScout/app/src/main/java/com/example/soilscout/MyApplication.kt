// app/src/main/java/com/example/soilscout/MyApplication.kt
package com.example.soilscout

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import java.util.concurrent.TimeUnit
import android.util.Log
import com.example.soilscout.data.AppDatabase
import com.example.soilscout.data.NotificationDao
import com.example.soilscout.util.Constants
import com.example.soilscout.util.LocaleManager // Import LocaleManager


object SessionManager {
    private const val USER_TOKEN_KEY = "token"


    private val prefs: SharedPreferences by lazy {
        MyApplication.appContext.getSharedPreferences("auth", Context.MODE_PRIVATE)
    }

    fun saveAuthToken(token: String) {
        prefs.edit().putString(USER_TOKEN_KEY, token).apply()
        Log.d("SessionManager", "Auth token saved.")
    }

    fun getUserToken(): String? {
        val token = prefs.getString(USER_TOKEN_KEY, null)
        Log.d("SessionManager", "Get token: ${token?.take(10)}...")
        return token
    }

    fun clearAuthToken() {
        prefs.edit().remove(USER_TOKEN_KEY).apply()
        Log.d("SessionManager", "Auth token cleared.")
    }

    fun isLoggedIn(): Boolean {
        val loggedIn = getUserToken() != null
        Log.d("SessionManager", "Is logged in: $loggedIn")
        return loggedIn
    }
}

class MyApplication : Application() {

    companion object {
        lateinit var appContext: Context
        lateinit var okHttpClient: OkHttpClient
        lateinit var database: AppDatabase
        lateinit var notificationDao: NotificationDao

        const val BASE_API_URL = "http://10.0.2.2:5000/api"
        val BASE_IMAGE_URL = "http://10.0.2.2:5000"
    }

    override fun onCreate() {
        super.onCreate()
        initializeOkHttpClient()
        database = AppDatabase.getDatabase(this)
        notificationDao = database.notificationDao()
        createNotificationChannel()
    }

    override fun attachBaseContext(base: Context?) {
        base?.let {
            LocaleManager.init(it)
            appContext = it
            super.attachBaseContext(LocaleManager.applyLocale(it))
        } ?: super.attachBaseContext(base)
    }


    private fun initializeOkHttpClient() {
        val authInterceptor = object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val originalRequest = chain.request()
                val token = SessionManager.getUserToken()

                val requestBuilder = originalRequest.newBuilder()
                token?.let {
                    requestBuilder.header("Authorization", "Bearer $it")
                }
                val request = requestBuilder.build()

                val response = chain.proceed(request)

                if (response.code == 401) {
                    Log.w("MyApplication", "Unauthorized access for ${response.request.url}. Clearing token and redirecting.")

                    response.close()

                    SessionManager.clearAuthToken()

                    val loginIntent = Intent(applicationContext, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    applicationContext.startActivity(loginIntent)
                }
                return response
            }
        }

        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = Constants.NOTIFICATION_CHANNEL_NAME
            val descriptionText = "Сповіщення про критичні показники Soil Scout"

            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("MyApplication", "Notification channel '${Constants.NOTIFICATION_CHANNEL_ID}' created.")
        }
    }
}