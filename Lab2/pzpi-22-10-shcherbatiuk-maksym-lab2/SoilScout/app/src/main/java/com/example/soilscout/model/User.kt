package com.example.soilscout.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("is_admin")
    val is_admin: Boolean,

    @SerializedName("profile_picture_url")
    val profile_picture_url: String?


)