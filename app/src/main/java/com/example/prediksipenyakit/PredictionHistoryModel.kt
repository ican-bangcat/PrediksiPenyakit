package com.example.prediksipenyakit

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PredictionHistoryModel(
    @SerialName("user_id") val userId: String,
// TAMBAHAN BARU DI SINI 👇
    @SerialName("created_at") val createdAt: String? = null,
    // Hasil Prediksi
    @SerialName("prediction_result") val predictionResult: Int,
    @SerialName("risk_score") val riskScore: Float,

    // Snapshot Data Lengkap
    val age: Int,
    val gender: String,
    val bmi: Float,

    @SerialName("systolic_bp") val systolicBp: Int,
    @SerialName("diastolic_bp") val diastolicBp: Int,
    @SerialName("heart_rate") val heartRate: Int,

    @SerialName("daily_steps") val dailySteps: Int,
    @SerialName("sleep_hours") val sleepHours: Float,
    @SerialName("water_intake") val waterIntake: Float,
    @SerialName("calories_consumed") val calories: Int,

    val smoker: Int,
    val alcohol: Int,
    @SerialName("family_history") val familyHistory: Int
)