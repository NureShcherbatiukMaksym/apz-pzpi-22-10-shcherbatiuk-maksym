package com.example.soilscout.model

data class Field(
    val id: Int,
    val user_id: Int,
    val name: String,
    val area: Float?,
    val geo_zone: String,
    val selected: Boolean,
    val created_at: String
)

