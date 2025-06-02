package com.example.soilscout.model

import java.util.Date

data class Measurement(
    val id: Int,
    val point_id: Int,
    val sensor_id: Int,
    val type: String,
    val value: Float,
    val timestamp: Date? = null
)