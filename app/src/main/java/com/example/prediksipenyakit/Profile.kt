package com.example.prediksipenyakit

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val role: String? = "user" // Defaultnya user kalau null
)