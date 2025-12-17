package com.example.prediksipenyakit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)

        // Klik Sign Up -> Pindah ke Login (simulasi flow registrasi)
        btnSignUp.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Klik Text Login -> Pindah ke Login
        tvGoToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}