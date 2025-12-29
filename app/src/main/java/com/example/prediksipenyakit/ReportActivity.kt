package com.example.prediksipenyakit

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ReportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        // 1. Setup Tombol Back
        val btnBack = findViewById<ImageView>(R.id.btnBackReport)
        btnBack.setOnClickListener {
            finish() // Kembali ke halaman sebelumnya
        }

        // 2. Tampilkan Data
        val userInput = intent.getParcelableExtra<UserInputModel>("USER_DATA")
        val tvReport = findViewById<TextView>(R.id.tvFullReport)

        userInput?.let {
            val report = """
                DATA PRIBADI
                • Usia: ${it.age} Tahun
                • Gender: ${it.gender}
                • Riwayat Keluarga: ${if (it.familyHistory == 1) "Ada" else "Tidak Ada"}
                
                FISIK & METABOLISME
                • Tinggi/Berat: ${String.format("%.1f", it.bmi)} (BMI)
                • Tensi: ${it.systolicBp}/${it.diastolicBp} mmHg
                • Detak Jantung: ${it.heartRate} bpm
                • Kalori: ${it.caloriesConsumed} kkal
                
                GAYA HIDUP
                • Rokok: ${if (it.smoker == 1) "Ya" else "Tidak"}
                • Alkohol: ${if (it.alcohol == 1) "Ya" else "Tidak"}
                • Langkah: ${it.dailySteps} / hari
                • Tidur: ${it.sleepHours} jam
                • Air: ${it.waterIntake} Liter
            """.trimIndent()
            tvReport.text = report
        }
    }
}