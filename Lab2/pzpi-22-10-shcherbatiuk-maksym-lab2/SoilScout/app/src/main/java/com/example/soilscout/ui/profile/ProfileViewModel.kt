package com.example.soilscout.ui.profile

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.soilscout.MyApplication
import com.example.soilscout.SessionManager
import com.example.soilscout.model.User
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.UnknownHostException

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val okHttpClient: OkHttpClient = MyApplication.okHttpClient
    private val BASE_URL = MyApplication.BASE_API_URL

    private val _userProfile = MutableLiveData<ResultWrapper<User>>()
    val userProfile: LiveData<ResultWrapper<User>> = _userProfile

    private val _updateProfileResult = MutableLiveData<ResultWrapper<User>>()
    val updateProfileResult: LiveData<ResultWrapper<User>> = _updateProfileResult

    private val _uploadImageResult = MutableLiveData<ResultWrapper<String>>()
    val uploadImageResult: LiveData<ResultWrapper<String>> = _uploadImageResult

    private val _validationError = MutableLiveData<String?>()
    val validationError: LiveData<String?> = _validationError

    private var currentUserId: String? = null
    private var _selectedImageUri: Uri? = null // Додано для тимчасового зберігання URI

    fun setSelectedImageUri(uri: Uri?) {
        _selectedImageUri = uri
    }

    fun fetchUserProfile() {
        _userProfile.value = ResultWrapper.Loading
        viewModelScope.launch {
            try {
                val token = SessionManager.getUserToken()
                if (token == null) {
                    _userProfile.postValue(ResultWrapper.Error("Користувач не авторизований."))
                    return@launch
                }

                val request = Request.Builder()
                    .url("$BASE_URL/users/me")
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()

                val result = withContext(Dispatchers.IO) {
                    val response = okHttpClient.newCall(request).execute()
                    val responseBodyString = response.body?.string()

                    if (response.isSuccessful && responseBodyString != null) {
                        Log.d("ProfileViewModel", "Fetch Profile Success: $responseBodyString")
                        val user = Gson().fromJson(responseBodyString, User::class.java)
                        currentUserId = user.id
                        ResultWrapper.Success(user)
                    } else {
                        val errorMsg = parseErrorMessage(responseBodyString, "Помилка ${response.code}")
                        Log.e("ProfileViewModel", "Fetch Error: $errorMsg, Code: ${response.code}, OriginalBody: $responseBodyString")
                        ResultWrapper.Error(errorMsg)
                    }
                }
                _userProfile.postValue(result)

            } catch (e: JsonSyntaxException) {
                Log.e("ProfileViewModel", "Fetch Profile JSON Parsing Error", e)
                _userProfile.postValue(ResultWrapper.Error("Помилка розбору даних профілю."))
            } catch (e: UnknownHostException) {
                Log.e("ProfileViewModel", "Fetch Profile Network Error", e)
                _userProfile.postValue(ResultWrapper.Error("Помилка мережі. Перевірте підключення до сервера."))
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Fetch Profile Exception", e)
                _userProfile.postValue(ResultWrapper.Error("Невідома помилка при завантаженні профілю: ${e.localizedMessage}"))
            }
        }
    }

    fun updateUserProfile(name: String, email: String, currentPasswordValue: String, newPasswordValue: String, confirmPasswordValue: String) {
        if (currentUserId == null) {
            _updateProfileResult.value = ResultWrapper.Error("ID користувача невідомий. Неможливо зберегти. Спробуйте оновити профіль.")
            return
        }

        if (!validateForm(name, email, currentPasswordValue, newPasswordValue, confirmPasswordValue)) {
            return
        }

        _updateProfileResult.value = ResultWrapper.Loading
        viewModelScope.launch {
            try {
                val token = SessionManager.getUserToken()
                if (token == null) {
                    _updateProfileResult.postValue(ResultWrapper.Error("Користувач не авторизований для збереження."))
                    return@launch
                }

                val requestBodyBuilder = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("name", name)
                    .addFormDataPart("email", email)

                _selectedImageUri?.let { uri ->
                    val file = uriToFile(uri, getApplication())
                    if (file != null) {
                        val fileBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                        requestBodyBuilder.addFormDataPart("profile_picture", file.name, fileBody)
                    } else {
                        Log.w("ProfileViewModel", "Could not convert selected URI to File for upload.")
                    }
                }

                if (newPasswordValue.isNotEmpty()) {
                    if (currentPasswordValue.isNotEmpty()) {
                        requestBodyBuilder.addFormDataPart("currentPassword", currentPasswordValue)
                    }
                    requestBodyBuilder.addFormDataPart("newPassword", newPasswordValue)
                    requestBodyBuilder.addFormDataPart("confirmPassword", confirmPasswordValue)
                }

                val request = Request.Builder()
                    .url("$BASE_URL/users/$currentUserId")
                    .addHeader("Authorization", "Bearer $token")
                    .put(requestBodyBuilder.build())
                    .build()

                val response = withContext(Dispatchers.IO) {
                    okHttpClient.newCall(request).execute()
                }

                val responseBodyString = response.body?.string()

                if (response.isSuccessful && responseBodyString != null) {
                    Log.d("ProfileViewModel", "Update Profile Success: $responseBodyString")
                    val updatedUser = Gson().fromJson(responseBodyString, User::class.java)
                    _updateProfileResult.postValue(ResultWrapper.Success(updatedUser))
                    _selectedImageUri = null // Очищаємо URI після успішного завантаження
                } else {
                    val errorMsg = parseErrorMessage(responseBodyString, "Помилка збереження ${response.code}")
                    Log.e("ProfileViewModel", "Update Error: $errorMsg, Code: ${response.code}, OriginalBody: $responseBodyString")
                    _updateProfileResult.postValue(ResultWrapper.Error(errorMsg))
                }
            } catch (e: JsonSyntaxException) {
                Log.e("ProfileViewModel", "Update Profile JSON Parsing Error", e)
                _updateProfileResult.postValue(ResultWrapper.Error("Помилка розбору оновлених даних."))
            } catch (e: UnknownHostException) {
                Log.e("ProfileViewModel", "Update Profile Network Error", e)
                _updateProfileResult.postValue(ResultWrapper.Error("Помилка мережі при збереженні."))
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Update Profile Exception", e)
                _updateProfileResult.postValue(ResultWrapper.Error("Невідома помилка при збереженні: ${e.localizedMessage}"))
            }
        }
    }

    private fun validateForm(name: String, email: String, currentPw: String, newPw: String, confirmPw: String): Boolean {
        _validationError.value = null

        if (name.isBlank()) {
            _validationError.value = "Ім'я не може бути порожнім."
            return false
        }

        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _validationError.value = "Введіть коректний Email."
            return false
        }

        if (newPw.isNotEmpty()) {
            if (currentPw.isBlank()) {
                _validationError.value = "Поточний пароль потрібен для зміни пароля."
                return false
            }
            if (newPw.length < 6) {
                _validationError.value = "Новий пароль має містити щонайменше 6 символів."
                return false
            }
            if (newPw != confirmPw) {
                _validationError.value = "Новий пароль та підтвердження не співпадають."
                return false
            }
        }
        return true
    }

    private fun uriToFile(uri: Uri, applicationContext: Application): File? {
        return try {
            val contentResolver = applicationContext.contentResolver
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            inputStream?.use { input ->
                var fileExtension = ".jpg"
                val mimeType = contentResolver.getType(uri)
                if (mimeType != null) {
                    val ext = android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
                    if (ext != null) {
                        fileExtension = ".$ext"
                    }
                }
                val fileName = "profile_upload_${System.currentTimeMillis()}$fileExtension"
                val file = File(applicationContext.cacheDir, fileName)
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
                file
            }
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error converting URI to File", e)
            null
        }
    }

    private fun parseErrorMessage(responseBody: String?, defaultMessage: String): String {
        return try {
            if (responseBody != null) {
                val gson = Gson()
                val errorResponse = gson.fromJson(responseBody, ErrorResponseMessage::class.java)
                errorResponse.message ?: defaultMessage
            } else {
                defaultMessage
            }
        } catch (e: Exception) {
            defaultMessage
        }
    }

    data class ErrorResponseMessage(val message: String?)

    fun clearValidationError() {
        _validationError.value = null
    }
}