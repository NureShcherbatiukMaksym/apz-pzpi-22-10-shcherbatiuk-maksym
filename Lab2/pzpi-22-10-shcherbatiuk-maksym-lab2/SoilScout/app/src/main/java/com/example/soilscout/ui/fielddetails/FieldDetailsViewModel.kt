// src/main/java/com/example/soilscout/ui/fielddetails/FieldDetailsViewModel.kt
package com.example.soilscout.ui.fielddetails

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.soilscout.MainActivity
import com.example.soilscout.MyApplication
import com.example.soilscout.R
import com.example.soilscout.model.Field
import com.example.soilscout.model.MeasurementPoint
import com.example.soilscout.ui.dashboard.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request

import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

import io.socket.client.IO
import io.socket.client.Socket
import java.net.URI
import io.socket.emitter.Emitter

import com.example.soilscout.model.Notification
import com.example.soilscout.util.Constants


class FieldDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private var fieldId: Int = -1
    private val _fieldDetails = MutableLiveData<Field?>()
    val fieldDetails: LiveData<Field?> = _fieldDetails
    private val _points = MutableLiveData<List<MeasurementPoint>>()
    val points: LiveData<List<MeasurementPoint>> = _points
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    private val _socketStatus = MutableLiveData<String>()
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    val socketStatus: LiveData<String> = _socketStatus

    private var socket: Socket? = null

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


    fun init(id: Int) {
        if (this.fieldId == id && _fieldDetails.value != null && _points.value != null && socket != null && socket?.connected() == true) {
            Log.d(
                "FieldDetailsViewModel",
                "ViewModel already initialized for field $id and socket is connected."
            )
            return
        }

        if (this.fieldId != id) {
            Log.d("FieldDetailsViewModel", "Initializing for new field ID: $id")
            disconnectWebSocket()
        } else {
            Log.d("FieldDetailsViewModel", "Re-initializing for field ID: $id")
            disconnectWebSocket()
        }


        this.fieldId = id
        _fieldDetails.value = null
        _points.value = emptyList()
        _error.value = null
        _isLoading.value = true
        _socketStatus.value = "Не підключено"


        setupWebSocket(fieldId)


        viewModelScope.launch {
            val fieldResult = fetchFieldDetailsFromServer(fieldId)
            if (fieldResult is Result.Success) {
                _fieldDetails.postValue(fieldResult.data)
                _error.postValue(null)
                Log.d("FieldDetailsViewModel", "Field details fetched successfully.")
            } else {
                if (fieldResult is Result.Error) {
                    _error.postValue("Помилка завантаження даних поля: ${fieldResult.message}")
                } else {
                    _error.postValue("Невідома помилка завантаження даних поля.")
                }
                _fieldDetails.postValue(null)
            }

            val pointsResult = fetchPointsFromServer(fieldId)
            _isLoading.postValue(false)

            if (pointsResult is Result.Success) {
                _points.postValue(pointsResult.data)
                _error.postValue(null)
                Log.d(
                    "FieldDetailsViewModel",
                    "Points fetched successfully."
                )
            } else if (pointsResult is Result.Error) {
                _points.postValue(emptyList())
                _error.postValue("Помилка завантаження точок: ${pointsResult.message}")
                Log.e(
                    "FieldDetailsViewModel",
                    "Failed to fetch points: ${pointsResult.message}"
                )
            } else {
                _points.postValue(emptyList())
                _error.postValue("Невідома помилка завантаження точок.")
                Log.e(
                    "FieldDetailsViewModel",
                    "Failed to fetch points: Unknown Result type"
                )
            }
            selectFieldOnServer(fieldId)
        }
    }


    private suspend fun fetchFieldDetailsFromServer(id: Int): Result<Field> {
        return withContext(Dispatchers.IO) {
            val client = MyApplication.okHttpClient
            val request =
                Request.Builder().url("${MyApplication.BASE_API_URL}/fields/${id}").get().build() // Adjusted to use MyApplication.BASE_API_URL
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    val fieldJson = JSONObject(responseBody)
                    val field = Field(
                        id = fieldJson.getInt("id"),
                        user_id = fieldJson.optInt("user_id"),
                        name = fieldJson.getString("name"),
                        area = fieldJson.getDouble("area").toFloat(),
                        geo_zone = fieldJson.getString("geo_zone"),
                        selected = fieldJson.optBoolean("selected"),
                        created_at = fieldJson.optString("created_at")
                    )
                    Result.Success(field)
                } else {
                    Result.Error("Помилка ${response.code}: ${response.message}. Body: ${responseBody ?: "null"}")
                }
            } catch (e: IOException) {
                Log.e("FieldDetailsViewModel", "IOException fetching field details", e)
                Result.Error("Помилка мережі: ${e.message}")
            } catch (e: Exception) {
                Log.e("FieldDetailsViewModel", "Exception fetching field details", e)
                Result.Error("Помилка парсингу або інша помилка: ${e.message ?: "Невідома помилка"}")
            }
        }
    }


    private suspend fun fetchPointsFromServer(fieldId: Int): Result<List<MeasurementPoint>> {
        return withContext(Dispatchers.IO) {
            val client = MyApplication.okHttpClient
            val request = Request.Builder()
                .url("${MyApplication.BASE_API_URL}/measurement-points/field/${fieldId}").get() // Adjusted to use MyApplication.BASE_API_URL
                .build()
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val pointsJsonArray = JSONArray(responseBody)
                    val pointsList = mutableListOf<MeasurementPoint>()
                    for (i in 0 until pointsJsonArray.length()) {
                        val pointJson = pointsJsonArray.getJSONObject(i)
                        val pointId = pointJson.getInt("id")

                        val point = MeasurementPoint(
                            id = pointId,
                            field_id = pointJson.optInt("field_id"),
                            point_order = pointJson.optInt("point_order"),
                            latitude = pointJson.getDouble("latitude"),
                            longitude = pointJson.getDouble("longitude"),
                            active = pointJson.optBoolean("active", false),
                            latestSoilMoisture = null,
                            latestTemperature = null,
                            latestAcidity = null
                        )

                        val latestMeasurementsJsonArray = pointJson.optJSONArray("latest_measurements")
                        if (latestMeasurementsJsonArray != null) {
                            for (j in 0 until latestMeasurementsJsonArray.length()) {
                                val measurementJson = latestMeasurementsJsonArray.getJSONObject(j)
                                val sensorId = measurementJson.optInt("sensor_id")
                                val value = measurementJson.optDouble("value").toFloat()

                                when (sensorId) {
                                    1 -> point.latestSoilMoisture = value.toInt()
                                    2 -> point.latestTemperature = value
                                    3 -> point.latestAcidity = value
                                }
                            }
                        }
                        pointsList.add(point)
                    }
                    Result.Success(pointsList)
                } else {
                    if (response.code == 404) {
                        Log.w("FieldDetailsViewModel", "No points found for field ${fieldId}. Response body: ${responseBody ?: "null"}")
                        Result.Success(emptyList())
                    } else {
                        Log.e("FieldDetailsViewModel", "Error fetching points: ${response.code}. Body: ${responseBody ?: "null"}")
                        Result.Error("Помилка ${response.code}: ${response.message}. Body: ${responseBody ?: "null"}")
                    }
                }
            } catch (e: IOException) {
                Log.e("FieldDetailsViewModel", "IOException fetching points", e)
                Result.Error("Помилка мережі при завантаженні точок: ${e.message}")
            } catch (e: Exception) {
                Log.e("FieldDetailsViewModel", "Exception fetching points", e)
                Result.Error("Помилка парсингу точок або інша помилка: ${e.message ?: "Невідома помилка"}")
            }
        }
    }

    private suspend fun selectFieldOnServer(fieldId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val client = MyApplication.okHttpClient
                val url = "${MyApplication.BASE_API_URL}/fields/select/"
                val json = JSONObject().apply { put("fieldId", fieldId) }
                val body = okhttp3.RequestBody.create(
                    "application/json; charset=utf-8".toMediaTypeOrNull(),
                    json.toString()
                )
                val request = Request.Builder().url(url).post(body).build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful) {
                    Log.d(
                        "FieldDetailsViewModel",
                        "Field $fieldId selected successfully on server."
                    )
                    Result.Success(Unit)
                } else {
                    Log.e(
                        "FieldDetailsViewModel",
                        "Failed to select field $fieldId: ${response.code}. Body: ${responseBody ?: "null"}"
                    )
                    Result.Error("Помилка ${response.code}: ${responseBody ?: "Немає тіла відповіді"}")
                }
            } catch (e: Exception) {
                Log.e("FieldDetailsViewModel", "Exception during select field API call", e)
                Result.Error("Помилка мережі або інша помилка при виборі поля: ${e.message ?: "Невідома помилка"}")
            }
        }
    }


    fun deselectField() {
        if (fieldId != -1) {
            viewModelScope.launch {
                deselectFieldOnServer(fieldId)
                disconnectWebSocket()
            }
        } else {
            Log.e("FieldDetailsViewModel", "Cannot deselect field, fieldId is -1")
        }
    }

    private suspend fun deselectFieldOnServer(fieldId: Int): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val client = MyApplication.okHttpClient
                val url = "${MyApplication.BASE_API_URL}/fields/deselect/"
                val json = JSONObject().apply { put("fieldId", fieldId) }
                val body = okhttp3.RequestBody.create(
                    "application/json; charset=utf-8".toMediaTypeOrNull(),
                    json.toString()
                )
                val request = Request.Builder().url(url).post(body).build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful) {
                    Log.d(
                        "FieldDetailsViewModel",
                        "Field $fieldId deselected successfully on server."
                    )
                    Result.Success(Unit)
                } else {
                    Log.e(
                        "FieldDetailsViewModel",
                        "Failed to deselect field $fieldId: ${response.code}. Body: ${responseBody ?: "null"}"
                    )
                    Result.Error("Помилка ${response.code}: ${responseBody ?: "Немає тіла відповіді"}")
                }
            } catch (e: Exception) {
                Log.e("FieldDetailsViewModel", "Exception during deselect field API call", e)
                Result.Error("Помилка мережі або інша помилка при деселекту поля: ${e.message ?: "Невідома помилка"}")
            }
        }
    }


    fun activatePoint(pointId: Int) {
        if (this.fieldId == -1) {
            Log.e("FieldDetailsViewModel", "Cannot activate point, fieldId is -1")
            _error.postValue("Помилка: ID поля не визначено.")
            return
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val client = MyApplication.okHttpClient
                    val url = "${MyApplication.BASE_API_URL}/measurement-points/activate"

                    val json = JSONObject().apply {
                        put("pointId", pointId)
                        put("fieldId", this@FieldDetailsViewModel.fieldId)
                    }
                    val body = okhttp3.RequestBody.create(
                        "application/json; charset=utf-8".toMediaTypeOrNull(),
                        json.toString()
                    )
                    val request = Request.Builder().url(url).post(body).build()

                    Log.d(
                        "FieldDetailsViewModel",
                        "Sending activate API call for point $pointId, field ${this@FieldDetailsViewModel.fieldId}. Payload: $json"
                    )

                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string()

                    if (response.isSuccessful) {
                        Log.d(
                            "FieldDetailsViewModel",
                            "Activate API call successful for point $pointId."
                        )
                        _error.postValue(null)
                    } else {
                        Log.e(
                            "FieldDetailsViewModel",
                            "Activate API call failed for point $pointId: ${response.code}. Body: ${responseBody ?: "null"}"
                        )
                        _error.postValue("Помилка активації точки: ${response.code}. Body: ${responseBody ?: ""}")
                    }
                } catch (e: Exception) {
                    Log.e("FieldDetailsViewModel", "Exception during point activation API call", e)
                    _error.postValue("Помилка мережі або інша помилка активації: ${e.message ?: "Невідома помилка"}")
                }
            }
        }
    }


    fun deactivatePoint(pointId: Int) {
        if (this.fieldId == -1) {
            Log.e("FieldDetailsViewModel", "Cannot deactivate point, fieldId is -1")
            _error.postValue("Помилка: ID поля не визначено.")
            return
        }

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val client = MyApplication.okHttpClient
                    val url = "${MyApplication.BASE_API_URL}/measurement-points/deactivate"

                    val json = JSONObject().apply {
                        put("pointId", pointId)
                        put("fieldId", this@FieldDetailsViewModel.fieldId)
                    }
                    val body = okhttp3.RequestBody.create(
                        "application/json; charset=utf-8".toMediaTypeOrNull(),
                        json.toString()
                    )
                    val request = Request.Builder().url(url).post(body).build()

                    Log.d(
                        "FieldDetailsViewModel",
                        "Sending deactivate API call for point $pointId, field ${this@FieldDetailsViewModel.fieldId}. Payload: $json"
                    )

                    val response = client.newCall(request).execute()
                    val responseBody = response.body?.string()

                    if (response.isSuccessful) {
                        Log.d(
                            "FieldDetailsViewModel",
                            "Deactivate API call successful for point $pointId."
                        )
                        _error.postValue(null)
                    } else {
                        Log.e(
                            "FieldDetailsViewModel",
                            "Deactivation failed for point $pointId, field ${this@FieldDetailsViewModel.fieldId}: ${response.code}. Body: ${responseBody ?: "null"}"
                        )
                        _error.postValue("Помилка деактивації точки: ${response.code}. Body: ${responseBody ?: ""}")
                    }
                } catch (e: Exception) {
                    Log.e(
                        "FieldDetailsViewModel",
                        "Exception during point deactivation API call",
                        e
                    )
                    _error.postValue("Помилка мережі або інша помилка деактивації: ${e.message ?: "Невідома помилка"}")
                }
            }
        }
    }


    private fun setupWebSocket(fieldId: Int) {
        if (socket != null && socket?.connected() == true) {
            Log.d("FieldDetailsViewModel", "WebSocket is already connected for field $fieldId. Skipping setup.")
            if (this.fieldId != -1 && this.fieldId != fieldId) {
                Log.d("FieldDetailsViewModel", "Field ID changed, re-subscribing to new field $fieldId")
                socket?.emit("unsubscribe", JSONObject().apply { put("fieldId", this@FieldDetailsViewModel.fieldId) })
                socket?.emit("subscribe", JSONObject().apply { put("fieldId", fieldId) })
                this.fieldId = fieldId
            } else if (this.fieldId == fieldId && socket?.connected() == true) {
                Log.d("FieldDetailsViewModel", "WebSocket already connected and subscribed to field $fieldId.")
            }
            return
        }

        try {
            val uri = URI(MyApplication.BASE_IMAGE_URL)

            val options = IO.Options.builder()
                .setTransports(arrayOf("websocket"))
                .setReconnection(true)
                .setReconnectionAttempts(5)
                .setReconnectionDelay(1000)
                .setReconnectionDelayMax(5000)
                .setTimeout(10000)
                .build()


            socket = IO.socket(uri, options)

            socket?.on(Socket.EVENT_CONNECT, onConnect)
            socket?.on(Socket.EVENT_DISCONNECT, onDisconnect)
            socket?.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
            socket?.on("update", onUpdate)

            socket?.connect()
            _socketStatus.postValue("Підключення...")

            Log.d("FieldDetailsViewModel", "Attempting to connect to WebSocket: $uri for field $fieldId")

        } catch (e: Exception) {
            Log.e("FieldDetailsViewModel", "Error setting up WebSocket for field $fieldId", e)
            _socketStatus.postValue("Помилка налаштування WebSocket")
            _error.postValue("Помилка налаштування WebSocket: ${e.message}")
        }
    }


    private val onConnect = Emitter.Listener {
        Log.d("FieldDetailsViewModel", "WebSocket Connected")
        _socketStatus.postValue("Підключено")
        if (fieldId != -1) {
            socket?.emit("subscribe", JSONObject().apply {
                put("fieldId", fieldId)
            })
            Log.d("FieldDetailsViewModel", "Emitted 'subscribe' event for field $fieldId")
        } else {
            Log.w("FieldDetailsViewModel", "WebSocket connected but fieldId is -1. Cannot subscribe.")
        }
    }

    private val onDisconnect = Emitter.Listener {
        Log.d("FieldDetailsViewModel", "WebSocket Disconnected")
        _socketStatus.postValue("Відключено")
    }

    private val onConnectError = Emitter.Listener { args ->
        val error = if (args.isNotEmpty() && args[0] is Exception) {
            args[0] as Exception
        } else {
            Exception("Unknown WebSocket connection error")
        }
        Log.e("FieldDetailsViewModel", "WebSocket Connection Error: ${error.message}", error)
        _socketStatus.postValue("Помилка підключення: ${error.message ?: "Невідома помилка"}")
    }

    private val onUpdate = Emitter.Listener { args ->
        if (args.isNotEmpty() && args[0] is JSONObject) {
            val data = args[0] as JSONObject
            Log.d("FieldDetailsViewModel", "Received WebSocket update: $data")

            try {
                when (data.optString("type")) {
                    "pointActivated" -> {
                        val updatedPointId = data.optInt("pointId", -1)
                        if (updatedPointId != -1 && data.optInt("fieldId", -2) == this.fieldId) {
                            _points.postValue(_points.value?.map {
                                if (it.id == updatedPointId) {
                                    Log.d(
                                        "FieldDetailsViewModel",
                                        "Point $updatedPointId activated via WebSocket"
                                    )
                                    it.copy(active = true)
                                } else {
                                    it
                                }
                            })
                        } else {
                            Log.w("FieldDetailsViewModel", "Received pointActivated for non-current field or invalid pointId. Current fieldId: ${this.fieldId}, Received: ${data.optInt("fieldId", -2)}, PointId: $updatedPointId")
                        }
                    }

                    "pointDeactivated" -> {
                        val updatedPointId = data.optInt("pointId", -1)
                        if (updatedPointId != -1 && data.optInt("fieldId", -2) == this.fieldId) {
                            _points.postValue(_points.value?.map {
                                if (it.id == updatedPointId) {
                                    Log.d(
                                        "FieldDetailsViewModel",
                                        "Point $updatedPointId deactivated via WebSocket"
                                    )
                                    it.copy(active = false)
                                } else {
                                    it
                                }
                            })
                        } else {
                            Log.w("FieldDetailsViewModel", "Received pointDeactivated for non-current field or invalid pointId. Current fieldId: ${this.fieldId}, Received: ${data.optInt("fieldId", -2)}, PointId: $updatedPointId")
                        }
                    }

                    "dataUpdated" -> {
                        val updatedPointId = data.optInt("pointId", -1)
                        val latestMeasurementsJsonArray = data.optJSONArray("latestMeasurements")

                        if (updatedPointId != -1 && latestMeasurementsJsonArray != null) {
                            val currentPoints = _points.value.orEmpty().toMutableList()
                            val index = currentPoints.indexOfFirst { it.id == updatedPointId }

                            if (index != -1) {
                                var pointToUpdate = currentPoints[index]

                                for (j in 0 until latestMeasurementsJsonArray.length()) {
                                    val measurementJson = latestMeasurementsJsonArray.getJSONObject(j)
                                    val sensorId = measurementJson.optInt("sensor_id")
                                    val value = measurementJson.optDouble("value").toFloat()


                                    pointToUpdate = when (sensorId) {
                                        1 -> pointToUpdate.copy(latestSoilMoisture = value.toInt())
                                        2 -> pointToUpdate.copy(latestTemperature = value)
                                        3 -> pointToUpdate.copy(latestAcidity = value)
                                        else -> pointToUpdate
                                    }

                                    checkSensorThresholdsAndNotify(
                                        sensorId = sensorId,
                                        value = value,
                                        pointId = updatedPointId,
                                        fieldId = this.fieldId,
                                        fieldName = _fieldDetails.value?.name
                                    )
                                }

                                currentPoints[index] = pointToUpdate
                                _points.postValue(currentPoints)
                                Log.d("FieldDetailsViewModel", "Point $updatedPointId data updated via WebSocket. New values: M: ${pointToUpdate.latestSoilMoisture}, T: ${pointToUpdate.latestTemperature}, A: ${pointToUpdate.latestAcidity}")

                            } else {
                                Log.w("FieldDetailsViewModel", "Received dataUpdated for point $updatedPointId not found in current points list.")
                            }
                        } else {
                            Log.w("FieldDetailsViewModel", "Received dataUpdated with invalid pointId ($updatedPointId) or missing latestMeasurements.")
                        }
                    }
                    else -> {
                        Log.w(
                            "FieldDetailsViewModel",
                            "Unknown WebSocket update type: ${data.optString("type")}"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("FieldDetailsViewModel", "Error processing WebSocket update", e)
                _error.postValue("Помилка обробки WebSocket повідомлення: ${e.message}")
            }
        } else {
            Log.w(
                "FieldDetailsViewModel",
                "Received non-JSONObject WebSocket update: ${args.firstOrNull()}"
            )
        }
    }


    private fun checkSensorThresholdsAndNotify(sensorId: Int, value: Float, pointId: Int, fieldId: Int, fieldName: String?) {
        val order = _points.value?.firstOrNull { it.id == pointId }?.point_order
        val pointIdentifier = if (order != null) "Точка №$order" else "Точка ID: $pointId"

        val fieldIdentifier = fieldName?.let { "Поле: '$it', " } ?: "Поле ID: $fieldId, "

        var notificationContent: String? = null
        var status: String? = null

        when (sensorId) {
            1 -> {
                val moisture = value.toInt()
                if (moisture < Constants.SOIL_MOISTURE_MIN || moisture > Constants.SOIL_MOISTURE_MAX) {
                    status = if (moisture < Constants.SOIL_MOISTURE_MIN) "Низька" else "Висока"
                    notificationContent = "${Constants.SENSOR_TYPE_SOIL_MOISTURE}: $status ($moisture%)"
                }
            }
            2 -> {
                if (value < Constants.TEMPERATURE_MIN || value > Constants.TEMPERATURE_MAX) {
                    status = if (value < Constants.TEMPERATURE_MIN) "Низька" else "Висока"
                    notificationContent = "${Constants.SENSOR_TYPE_TEMPERATURE}: $status (${value}°C)"
                }
            }
            3 -> {
                if (value < Constants.ACIDITY_MIN || value > Constants.ACIDITY_MAX) {
                    status = if (value < Constants.ACIDITY_MIN) "Низька" else "Висока"
                    notificationContent = "${Constants.SENSOR_TYPE_ACIDITY}: $status ($value pH)"
                }
            }
        }

        if (notificationContent != null && status != null) {
            val fullMessage = "$notificationContent на $pointIdentifier"
            val fullPointInfo = "$fieldIdentifier$pointIdentifier"
            createAndSaveNotification(
                message = fullMessage,
                titleSuffix = status,
                pointInfo = fullPointInfo,
                fieldId = fieldId,
                pointId = pointId
            )
        }
    }


    private fun createAndSaveNotification(message: String, titleSuffix: String, pointInfo: String, fieldId: Int, pointId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val dbNotification = com.example.soilscout.model.Notification(
                message = message,
                pointInfo = pointInfo,
                timestamp = System.currentTimeMillis(),
                status = titleSuffix,
                fieldId = fieldId,
                pointId = pointId
            )
            MyApplication.notificationDao.insertNotification(dbNotification)
            Log.d("FieldDetailsViewModel", "Сповіщення збережено в БД: $message")


            val systemNotificationId = System.currentTimeMillis().toInt()

            withContext(Dispatchers.Main) {
                val context = getApplication<MyApplication>().applicationContext

                val resultIntent = Intent(context, MainActivity::class.java).apply {

                }
                val pendingIntent: PendingIntent = PendingIntent.getActivity(
                    context,
                    systemNotificationId,
                    resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )


                val builder = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                    .setContentTitle("Soil Scout: Показник $titleSuffix")
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)


                try {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                            NotificationManagerCompat.from(context).notify(systemNotificationId, builder.build())
                            Log.d("FieldDetailsViewModel", "Системне сповіщення показано, ID: $systemNotificationId")
                        } else {
                            Log.w("FieldDetailsViewModel", "Системні сповіщення вимкнені користувачем.")
                        }
                    } else {
                        NotificationManagerCompat.from(context).notify(systemNotificationId, builder.build())
                        Log.d("FieldDetailsViewModel", "Системне сповіщення показано, ID: $systemNotificationId")
                    }
                } catch (e: SecurityException) {
                    // Це може статися, якщо дозвіл POST_NOTIFICATIONS не надано на Android 13+
                    Log.e("FieldDetailsViewModel", "SecurityException при показі сповіщення. Перевірте дозвіл POST_NOTIFICATIONS.", e)
                }
            }
        }
    }


    private fun disconnectWebSocket() {
        if (socket != null) {
            if (fieldId != -1 && socket?.connected() == true) {
                socket?.emit("unsubscribe", JSONObject().apply { put("fieldId", fieldId) })
                Log.d("FieldDetailsViewModel", "Emitted 'unsubscribe' event for field $fieldId")
            }
            socket?.off(Socket.EVENT_CONNECT, onConnect)
            socket?.off(Socket.EVENT_DISCONNECT, onDisconnect)
            socket?.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
            socket?.off("update", onUpdate)
            socket?.disconnect()
            socket = null
            _socketStatus.postValue("Відключено")
            Log.d("FieldDetailsViewModel", "WebSocket disconnected")
        }
    }

    override fun onCleared() {
        super.onCleared()
        disconnectWebSocket()
        Log.d("FieldDetailsViewModel", "ViewModel onCleared. WebSocket disconnected.")
    }

    fun selectField() {
        Log.d("FieldDetailsViewModel", "selectField() called. Init should have handled selection.")
    }
}