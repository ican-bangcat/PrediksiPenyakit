package com.example.prediksipenyakit

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileModel(
    val id: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,

    // TAMBAHKAN INI (Penting untuk logika Admin/User)
    // Kita kasih default value "user" untuk jaga-jaga jika datanya null
    @SerialName("role") val role: String = "user"
)