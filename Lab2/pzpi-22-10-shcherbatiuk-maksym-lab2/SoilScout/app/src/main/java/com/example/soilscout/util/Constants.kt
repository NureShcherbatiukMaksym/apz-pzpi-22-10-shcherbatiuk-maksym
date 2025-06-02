package com.example.soilscout.util

object Constants {

    const val BASE_URL = "http://10.0.2.2:5000/api/"
    const val BASE_IMAGE_URL = "http://10.0.2.2:5000"

    const val NOTIFICATION_CHANNEL_ID = "soil_scout_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Soil Scout Alerts"

    const val SOIL_MOISTURE_MIN = 70.0
    const val SOIL_MOISTURE_MAX = 90.0

    const val TEMPERATURE_MIN = 5.0
    const val TEMPERATURE_MAX = 30.0

    const val ACIDITY_MIN = 5.0
    const val ACIDITY_MAX = 7.5

    const val SENSOR_TYPE_SOIL_MOISTURE = "Вологість ґрунту"
    const val SENSOR_TYPE_TEMPERATURE = "Температура"
    const val SENSOR_TYPE_ACIDITY = "Кислотність"
}