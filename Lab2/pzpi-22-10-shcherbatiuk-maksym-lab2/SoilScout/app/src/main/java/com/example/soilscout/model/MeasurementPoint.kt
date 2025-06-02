package com.example.soilscout.model

data class MeasurementPoint(
    val id: Int,
    val field_id: Int,
    val point_order: Int,
    val latitude: Double,
    val longitude: Double,
    val active: Boolean,

    var latestSoilMoisture: Int? = null,
    var latestTemperature: Float? = null,
    var latestAcidity: Float? = null

)