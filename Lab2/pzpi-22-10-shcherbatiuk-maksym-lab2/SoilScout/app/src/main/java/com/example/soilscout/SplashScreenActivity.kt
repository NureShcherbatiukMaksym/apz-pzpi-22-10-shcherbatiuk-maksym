package com.example.soilscout

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.io.IOException
import android.util.Log

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val SPLASH_TIME_OUT: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        sharedPreferences = getSharedPreferences("auth", MODE_PRIVATE)
        val token = sharedPreferences.getString("token", null)

        if (token != null) {
            verifyTokenValidity(token)
        } else {
            navigateToLogin()
        }
    }

    private fun verifyTokenValidity(token: String) {
        val client = MyApplication.okHttpClient

        val request = Request.Builder()
            .url("http://10.0.2.2:5000/api/users/me")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("SplashScreen", "Token verification failed: ${e.message}")
                runOnUiThread {
                    navigateToLogin()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Всі операції, що оновлюють UI, залишаються тут
                runOnUiThread {
                    if (response.isSuccessful) {
                        Log.i("SplashScreen", "Token is valid. Navigating to Main.")
                        navigateToMain()
                    } else if (response.code == 401) {
                        Log.i("SplashScreen", "Token is invalid (401). Interceptor will handle redirect.")
                        finish()
                    } else {
                        Log.e("SplashScreen", "Token verification failed with code: ${response.code}")
                        navigateToLogin()
                    }
                }
                // Закривайте тіло відповіді тут, на фоновому потоці OkHttp
                response.body?.close()
            }
        })
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}