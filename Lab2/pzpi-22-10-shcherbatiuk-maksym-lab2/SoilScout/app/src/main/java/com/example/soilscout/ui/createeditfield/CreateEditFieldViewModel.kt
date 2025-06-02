package com.example.soilscout.ui.createeditfield

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.soilscout.MyApplication
import com.example.soilscout.model.Field
import com.example.soilscout.ui.dashboard.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException
import org.json.JSONArray

class CreateEditFieldViewModel(application: Application) : AndroidViewModel(application) {


    var fieldId: Int? = null

    private val _fieldDetails = MutableLiveData<Field?>()
    val fieldDetails: LiveData<Field?> = _fieldDetails

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _saveResult = MutableLiveData<Result<Unit>?>()
    val saveResult: LiveData<Result<Unit>?> = _saveResult

    private val _deleteResult = MutableLiveData<Result<Unit>?>()
    val deleteResult: LiveData<Result<Unit>?> = _deleteResult


    fun clearSaveResult() {
        _saveResult.value = null
    }

    fun clearDeleteResult() {
        _deleteResult.value = null
    }

    fun loadFieldDetails(id: Int) {
        fieldId = id
        _isLoading.value = true
        viewModelScope.launch {
            val result = fetchFieldDetailsFromServer(id)
            _isLoading.postValue(false)
            if (result is Result.Success) {
                _fieldDetails.postValue(result.data)
            } else if (result is Result.Error) {
                _fieldDetails.postValue(null)
                _saveResult.postValue(Result.Error("Не вдалося завантажити дані поля: ${result.message}"))
            }
        }
    }


    fun saveField(name: String, area: Float, geoZone: List<List<Double>>) {
        _isLoading.value = true
        _saveResult.value = null

        viewModelScope.launch {
            val payload = JSONObject().apply {
                put("name", name)
                put("area", area.toDouble())
                val geoZoneArray = JSONArray()
                val coordinatesArray = JSONArray()
                geoZone.forEach { point ->
                    val pointArray = JSONArray()
                    pointArray.put(point[0])
                    pointArray.put(point[1])
                    coordinatesArray.put(pointArray)
                }
                geoZoneArray.put(coordinatesArray)

                val geoJson = JSONObject().apply {
                    put("type", "Polygon")
                    put("coordinates", geoZoneArray)
                }
                put("geo_zone", geoJson)
            }

            val requestBody = RequestBody.create(
                "application/json; charset=utf-8".toMediaTypeOrNull(),
                payload.toString()
            )

            Log.d("SaveViewModel", "Request Payload: ${payload.toString()}")

            val url = if (fieldId == null) {
                "http://10.0.2.2:5000/api/fields"
            } else {
                "http://10.0.2.2:5000/api/fields/${fieldId}"
            }

            val request = if (fieldId == null) {
                Request.Builder().url(url).post(requestBody).build()
            } else {
                Request.Builder().url(url).put(requestBody).build()
            }

            val client = MyApplication.okHttpClient

            val result = try {

                withContext(Dispatchers.IO) {
                    Log.d("SaveViewModel", "Executing network request to $url on IO thread")
                    val response = client.newCall(request).execute()

                    Log.d("SaveViewModel", "Response received on IO thread. Code: ${response.code}, Successful: ${response.isSuccessful}")

                    val responseBody = response.body?.string()
                    Log.d("SaveViewModel", "Response Body read on IO thread: ${responseBody ?: "null"}")


                    if (response.isSuccessful) {
                        Log.d("SaveViewModel", "Response is successful. Preparing Success(Unit).")
                        Result.Success(Unit)
                    } else {
                        Log.e("SaveViewModel", "Response is NOT successful. Code: ${response.code}. Preparing Error.")
                        val errorMessage = "Помилка ${response.code}: ${response.message}. Body: ${responseBody ?: "null"}"
                        Result.Error(errorMessage)
                    }
                }

            } catch (e: IOException) {
                Log.e("SaveViewModel", "!!! IOException during network request !!!", e)
                Result.Error("Помилка мережі або запиту: ${e.message}")
            } catch (e: Exception) {
                Log.e("SaveViewModel", "!!! Generic Exception caught in try block !!!", e)
                Result.Error("Невідома помилка збереження: ${e.message}")
            }

            _isLoading.postValue(false)
            _saveResult.postValue(result)
        }
    }

    fun deleteField(id: Int) {
        _isLoading.value = true
        _deleteResult.value = null

        viewModelScope.launch {
            val url = "http://10.0.2.2:5000/api/fields/${id}"
            val request = Request.Builder().url(url).delete().build()

            val client = MyApplication.okHttpClient

            val result = try {
                withContext(Dispatchers.IO) {
                    Log.d("SaveViewModel", "Executing DELETE request to $url on IO thread")
                    val response = client.newCall(request).execute()
                    Log.d("SaveViewModel", "DELETE Response received on IO thread. Code: ${response.code}, Successful: ${response.isSuccessful}")
                    val responseBody = response.body?.string()
                    Log.d("SaveViewModel", "DELETE Response Body read on IO thread: ${responseBody ?: "null"}")


                    if (response.isSuccessful) {
                        Log.d("SaveViewModel", "DELETE Response is successful. Preparing Success(Unit).")
                        Result.Success(Unit)
                    } else {
                        Log.e("SaveViewModel", "DELETE Response is NOT successful. Code: ${response.code}. Preparing Error.")
                        val errorMessage = "Помилка видалення ${response.code}: ${response.message}. Body: ${responseBody ?: "null"}"
                        Result.Error(errorMessage)
                    }
                }

            } catch (e: IOException) {
                Log.e("SaveViewModel", "!!! IOException during DELETE request !!!", e)
                Result.Error("Помилка мережі або запиту при видаленні: ${e.message}")
            } catch (e: Exception) {
                Log.e("SaveViewModel", "!!! Generic Exception caught during DELETE !!!", e)
                Result.Error("Невідома помилка при видаленні: ${e.message}")
            }

            _isLoading.postValue(false)
            _deleteResult.postValue(result)
        }
    }



    private suspend fun fetchFieldDetailsFromServer(id: Int): Result<Field> {
        return withContext(Dispatchers.IO) {
            val client = MyApplication.okHttpClient
            val request = Request.Builder()
                .url("http://10.0.2.2:5000/api/fields/${id}")
                .get()
                .build()

            try {
                Log.d("SaveViewModel", "Executing GET request to fetch field $id on IO thread")
                val response = client.newCall(request).execute()
                Log.d("SaveViewModel", "GET Response received on IO thread. Code: ${response.code}, Successful: ${response.isSuccessful}")
                val responseBody = response.body?.string()
                Log.d("SaveViewModel", "GET Response Body read on IO thread: ${responseBody ?: "null"}")

                if (response.isSuccessful && responseBody != null) {
                    val fieldJson = JSONObject(responseBody)
                    Log.d("SaveViewModel", "Successfully parsed field JSON on IO thread.")
                    val field = Field(
                        id = fieldJson.getInt("id"),
                        user_id = fieldJson.getInt("user_id"),
                        name = fieldJson.getString("name"),
                        area = fieldJson.getDouble("area").toFloat(),
                        geo_zone = fieldJson.getString("geo_zone"),
                        selected = fieldJson.getBoolean("selected"),
                        created_at = fieldJson.getString("created_at")
                    )
                    Result.Success(field)
                } else {
                    Log.e("SaveViewModel", "GET Response is NOT successful or body is null. Code: ${response.code}")
                    val errorMessage = "Помилка завантаження поля ${response.code}: ${response.message}. Body: ${responseBody ?: "null"}"
                    Result.Error(errorMessage)
                }
            } catch (e: IOException) {
                if (e.message?.contains("Token expired", ignoreCase = true) == true) {
                    Log.e("SaveViewModel", "Token expired during field fetch (handled by Interceptor?)", e)
                    Result.Error("Сесія закінчилася. Будь ласка, увійдіть знову.")
                } else {
                    Log.e("SaveViewModel", "!!! IOException during GET field request !!!", e)
                    Result.Error("Помилка мережі або запиту: ${e.message}")
                }
            } catch (e: org.json.JSONException) {
                Log.e("SaveViewModel", "!!! JSONException parsing GET field response !!!", e)
                Result.Error("Помилка парсингу відповіді поля (JSON): ${e.message}")
            } catch (e: Exception) {
                Log.e("SaveViewModel", "!!! Generic Exception caught during GET field !!!", e)
                Result.Error("Невідома помилка завантаження поля: ${e.message}")
            }
        }
    }
}