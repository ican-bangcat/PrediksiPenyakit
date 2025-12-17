package com.example.prediksipenyakit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvGoToSignUp = findViewById<TextView>(R.id.tvGoToSignUp)

        // Klik Login -> Ke HomeActivity
        btnLogin.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish() // Agar user tidak bisa kembali ke login saat tekan back
        }

        // Klik Sign Up -> Ke RegisterActivity
        tvGoToSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}