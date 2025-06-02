package com.example.soilscout.ui.dashboard

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.*
import com.example.soilscout.MyApplication
import com.example.soilscout.model.Field
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.text.Collator


class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("auth", Context.MODE_PRIVATE)


    private val _userDataText = MutableLiveData<String>()
    val userDataText: LiveData<String> = _userDataText

    private val _allFields = MutableLiveData<List<Field>>()

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _searchTerm = MutableLiveData<String>("")
    val searchTerm: LiveData<String> = _searchTerm

    private val _sortBy = MutableLiveData<String>("name")
    val sortBy: LiveData<String> = _sortBy

    private val _sortOrder = MutableLiveData<String>("asc")
    val sortOrder: LiveData<String> = _sortOrder

    private val _filter = MutableLiveData<String>("all")
    val filter: LiveData<String> = _filter

    private val _displayedFields = MediatorLiveData<List<Field>>()
    val displayedFields: LiveData<List<Field>> = _displayedFields


    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())


    init {
        _displayedFields.addSource(_allFields) { updateDisplayedFields() }
        _displayedFields.addSource(_searchTerm) { updateDisplayedFields() }
        _displayedFields.addSource(_sortBy) { updateDisplayedFields() }
        _displayedFields.addSource(_sortOrder) { updateDisplayedFields() }
        _displayedFields.addSource(_filter) { updateDisplayedFields() }


        fetchUserFields()
    }



    private fun updateDisplayedFields() {
        val currentFields = _allFields.value ?: emptyList()
        val currentSearchTerm = _searchTerm.value ?: ""
        val currentSortBy = _sortBy.value ?: "name"
        val currentSortOrder = _sortOrder.value ?: "asc"
        val currentFilter = _filter.value ?: "all"

        val searchedFields = if (currentSearchTerm.isBlank()) {
            currentFields
        } else {
            currentFields.filter {
                it.name.contains(currentSearchTerm, ignoreCase = true)
            }
        }

        val filteredFields = searchedFields.filter { field ->
            when (currentFilter) {
                "all" -> true
                "active" -> field.selected == true
                "inactive" -> field.selected == false
                else -> true
            }
        }

        val sortedFields = when (currentSortBy) {
            "name" -> {
                val ukrainianCollator = Collator.getInstance(Locale("uk", "UA"))
                filteredFields.sortedWith { a, b -> ukrainianCollator.compare(a.name, b.name) }
            }
            "area" -> filteredFields.sortedBy { it.area }
            "created_at" -> filteredFields.sortedBy {

                try {
                    dateFormat.parse(it.created_at)?.time ?: Long.MIN_VALUE
                } catch (e: Exception) {
                    Log.e("DashboardViewModel", "Error parsing date: ${it.created_at}", e)
                    Long.MIN_VALUE
                }
            }
            else -> filteredFields.sortedBy { it.name }
        }


        val finalSortedFields = if (currentSortOrder == "desc") {
            sortedFields.reversed()
        } else {
            sortedFields
        }


        _displayedFields.value = finalSortedFields
        Log.d("DashboardViewModel", "Updated displayed fields: ${finalSortedFields.size} items")
    }


    fun loadUserDataFromPrefs() { /* ... */ }


    fun fetchUserFields() {
        val token = sharedPreferences.getString("token", null)
        if (token == null) {
            _error.postValue("Помилка: Необхідно авторизуватися для завантаження полів.")
            Log.e("DashboardViewModel", "Cannot fetch fields: token is null")
            return
        }

        _isLoading.postValue(true)
        _error.postValue(null)

        viewModelScope.launch {
            val result = fetchFieldsFromServer(token)
            _isLoading.postValue(false)

            when (result) {
                is Result.Success -> {
                    _allFields.postValue(result.data)
                    Log.d("DashboardViewModel", "Successfully fetched ${result.data.size} total fields")

                }
                is Result.Error -> {
                    _allFields.postValue(emptyList())
                    _error.postValue(result.message)
                    Log.e("DashboardViewModel", "Error fetching fields: ${result.message}")
                }
            }
        }
    }

    // Функція для мережевого запиту (не змінюється)
    private suspend fun fetchFieldsFromServer(token: String): Result<List<Field>> {
        return withContext(Dispatchers.IO) {
            val client = MyApplication.okHttpClient
            val request = Request.Builder()
                .url("http://10.0.2.2:5000/api/fields")
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val jsonArray = JSONArray(responseBody)
                    val fieldsList = mutableListOf<Field>()
                    for (i in 0 until jsonArray.length()) {
                        val fieldJson = jsonArray.getJSONObject(i)
                        val field = Field(
                            id = fieldJson.getInt("id"),
                            user_id = fieldJson.optInt("user_id"),
                            name = fieldJson.getString("name"),
                            area = fieldJson.getDouble("area").toFloat(),
                            geo_zone = fieldJson.getString("geo_zone"),
                            selected = fieldJson.optBoolean("selected", false),
                            created_at = fieldJson.getString("created_at")
                        )
                        fieldsList.add(field)
                    }
                    Result.Success(fieldsList)
                } else {
                    val errorMessage = "Помилка ${response.code}: ${response.message}. Body: ${responseBody ?: "null"}"
                    if (response.code == 401) {
                        Result.Error("Помилка авторизації: Ваша сесія закінчилася.")
                    } else {
                        Result.Error(errorMessage)
                    }
                }
            } catch (e: IOException) {
                Log.e("DashboardViewModel", "Network error fetching fields", e)
                Result.Error("Помилка мережі або запиту: ${e.message ?: "Невідома помилка мережі"}")
            } catch (e: org.json.JSONException) {
                Log.e("DashboardViewModel", "JSON error fetching fields", e)
                Result.Error("Помилка обробки відповіді сервера (JSON): ${e.message ?: "Невідома помилка JSON"}")
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Unknown error fetching fields", e)
                Result.Error("Невідома помилка при завантаженні полів: ${e.message ?: "Невідома помилка"}")
            }
        }
    }

    fun setSearchTerm(term: String) {
        _searchTerm.value = term
    }

    fun setSortCriteria(criteria: String) {
        if (_sortBy.value != criteria) {
            _sortBy.value = criteria

        } else {
            toggleSortOrder()
        }
    }

    fun toggleSortOrder() {
        _sortOrder.value = if (_sortOrder.value == "asc") "desc" else "asc"
    }

    fun setFilter(filter: String) {
        _filter.value = filter
    }
}


sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}