package com.example.prediksipenyakit

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileModel(
    val id: String, // UUID dari Auth User
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String
)