package com.example.prediksipenyakit

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        // Pengaturan padding untuk layar penuh (Edge-to-Edge) bawaan project baru
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // --- LOGIKA TIMER 3 DETIK ---
        // 3000ms = 3 detik
        Handler(Looper.getMainLooper()).postDelayed({

            // 1. Buat Intent untuk pindah ke MainActivity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)

            // 2. Hapus SplashActivity dari daftar Back Stack
            // (Agar kalau user tekan tombol Back di menu utama, tidak kembali ke Splash)
            finish()

        }, 3000)
    }
}