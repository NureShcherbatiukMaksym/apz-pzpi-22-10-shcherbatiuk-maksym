package com.example.soilscout

import android.content.Context // Додайте цей імпорт
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.soilscout.util.LocaleManager // Додайте цей імпорт
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.Locale // Додайте цей імпорт

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    private lateinit var googleSignInButton: SignInButton
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    private val GOOGLE_SIGN_IN_REQUEST_CODE = 1001
    private var activityLocale: Locale? = null // Зберігатимемо поточну локаль Activity

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let {
            activityLocale = Locale(LocaleManager.getLanguage())
            LocaleManager.applyLocale(it)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (activityLocale == null) {
            activityLocale = Locale(LocaleManager.getLanguage())
        }

        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences("auth", MODE_PRIVATE) //
        val token = sharedPreferences.getString("token", null) //
        if (token != null) { //
            startActivity(Intent(this, MainActivity::class.java)) //
            finish() //
            return //
        }

        firebaseAuth = FirebaseAuth.getInstance() //

        emailInput = findViewById(R.id.editTextEmail) //
        passwordInput = findViewById(R.id.editTextPassword) //
        loginButton = findViewById(R.id.buttonLogin) //
        googleSignInButton = findViewById(R.id.buttonGoogleSignIn) //

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) //
            .requestIdToken("444351979020-q7rop34595tp4rovhk3l1uedn1mpb040.apps.googleusercontent.com") // WEB CLIENT ID
            .requestEmail() //
            .build() //

        googleSignInClient = GoogleSignIn.getClient(this, gso) //

        googleSignInButton.setOnClickListener { //
            googleSignInClient.signOut().addOnCompleteListener { //
                val signInIntent = googleSignInClient.signInIntent //
                startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST_CODE) //
            }
        }

        loginButton.setOnClickListener { //
            val email = emailInput.text.toString() //
            val password = passwordInput.text.toString() //
            loginUser(email, password) //
        }
    }

    override fun onResume() {
        super.onResume()
        val persistedLanguage = LocaleManager.getLanguage()
        if (activityLocale != null && activityLocale?.language != persistedLanguage) {
            // Якщо LoginActivity все ще активна і мова змінилася (малоймовірно, але для повноти)
            recreate()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { //
        super.onActivityResult(requestCode, resultCode, data) //

        if (requestCode == GOOGLE_SIGN_IN_REQUEST_CODE) { //
            val task = GoogleSignIn.getSignedInAccountFromIntent(data) //
            try {
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java) //
                firebaseAuthWithGoogle(account) //
            } catch (e: ApiException) { //
                Log.e("LoginActivity", "Google sign-in failed", e) //
                Toast.makeText(this, "Не вдалося увійти через Google", Toast.LENGTH_SHORT).show() //
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) { //
        val credential = GoogleAuthProvider.getCredential(account.idToken, null) //
        firebaseAuth.signInWithCredential(credential) //
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) { //
                    firebaseAuth.currentUser?.getIdToken(true) //
                        ?.addOnSuccessListener { result ->
                            val firebaseIdToken = result.token //
                            Log.d("LoginActivity", "Firebase ID token: $firebaseIdToken") //
                            sendGoogleTokenToBackend(firebaseIdToken) //
                        }
                } else { //
                    Log.e("LoginActivity", "Firebase auth failed", task.exception) //
                    Toast.makeText(this, "Помилка Firebase-автентифікації", Toast.LENGTH_SHORT).show() //
                }
            }
    }

    private fun sendGoogleTokenToBackend(idToken: String?) { //
        if (idToken == null) { //
            Toast.makeText(this, "Помилка: порожній ID токен", Toast.LENGTH_SHORT).show() //
            return //
        }

        val client = OkHttpClient() //
        val json = JSONObject() //
        json.put("token", idToken) //

        val requestBody = RequestBody.create( //
            "application/json; charset=utf-8".toMediaTypeOrNull(), //
            json.toString() //
        )

        val request = Request.Builder() //
            .url("http://10.0.2.2:5000/api/auth/login/google") //
            .post(requestBody) //
            .build() //

        client.newCall(request).enqueue(object : Callback { //
            override fun onFailure(call: Call, e: IOException) { //
                runOnUiThread { //
                    Toast.makeText(this@LoginActivity, "Помилка входу через Google", Toast.LENGTH_SHORT).show() //
                }
            }

            override fun onResponse(call: Call, response: Response) { //
                handleLoginResponse(response) //
            }
        })
    }

    private fun loginUser(email: String, password: String) { //
        val client = OkHttpClient() //
        val json = JSONObject() //
        json.put("email", email) //
        json.put("password", password) //

        val requestBody = RequestBody.create( //
            "application/json; charset=utf-8".toMediaTypeOrNull(), //
            json.toString() //
        )

        val request = Request.Builder() //
            .url("http://10.0.2.2:5000/api/auth/login/password") //
            .addHeader("Content-Type", "application/json") //
            .post(requestBody) //
            .build() //

        client.newCall(request).enqueue(object : Callback { //
            override fun onFailure(call: Call, e: IOException) { //
                runOnUiThread { //
                    Toast.makeText(this@LoginActivity, "Помилка з'єднання з сервером", Toast.LENGTH_SHORT).show() //
                }
            }

            override fun onResponse(call: Call, response: Response) { //
                handleLoginResponse(response) //
            }
        })
    }

    private fun handleLoginResponse(response: Response) { //
        val responseBodyString = response.body?.string() //

        if (response.isSuccessful && responseBodyString != null) { //
            try {
                val jsonResponse = JSONObject(responseBodyString) //
                val token = jsonResponse.getString("token") //

                SessionManager.saveAuthToken(token) //
                runOnUiThread { //
                    Toast.makeText(this, "Успішний вхід", Toast.LENGTH_SHORT).show() //
                    startActivity(Intent(this, MainActivity::class.java)) //
                    finish() //
                }

            } catch (e: JSONException) { //
                Log.e("LoginActivity", "JSON parse error", e) //
                runOnUiThread { //
                    Toast.makeText(this, "Помилка обробки відповіді", Toast.LENGTH_SHORT).show() //
                }
            }
        } else { //
            runOnUiThread { //
                Toast.makeText(this, "Помилка входу. Код: ${response.code}", Toast.LENGTH_SHORT).show() //
            }
        }
    }
}