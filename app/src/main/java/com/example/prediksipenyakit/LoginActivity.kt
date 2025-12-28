package com.example.prediksipenyakit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvGoToSignUp = findViewById<TextView>(R.id.tvGoToSignUp)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Mohon isi email dan password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            btnLogin.text = "Loading..."

            lifecycleScope.launch {
                try {
                    // 1. PROSES LOGIN (Cek Email & Password)
                    SupabaseClient.client.auth.signInWith(Email) {
                        this.email = email
                        this.password = password
                    }

                    // 2. CEK ROLE (Ambil data dari tabel 'profiles')
                    val user = SupabaseClient.client.auth.currentUserOrNull()
                    if (user != null) {
                        val userId = user.id

                        // Query ke database: Cari profile milik user ini
                        val profile = SupabaseClient.client.postgrest["profiles"]
                            .select {
                                filter {
                                    eq("id", userId)
                                }
                            }.decodeSingle<Profile>()

                        Toast.makeText(applicationContext, "Login Berhasil! Role: ${profile.role}", Toast.LENGTH_SHORT).show()

                        // 3. PENGALIHAN HALAMAN BERDASARKAN ROLE
                        if (profile.role == "admin") {
                            // Jika Admin -> Masuk AdminActivity
                            val intent = Intent(this@LoginActivity, AdminActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        } else {
                            // Jika User Biasa -> Masuk HomeActivity (Dashboard)
                            // Catatan: Sesuai diskusi sebelumnya, User harusnya ke HomeActivity dulu, bukan langsung Prediksi
                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    // Pesan error lebih detail jika perlu
                    val errorMsg = if (e.message?.contains("Invalid login credentials") == true)
                        "Email atau Password Salah" else "Gagal Login: ${e.message}"

                    Toast.makeText(applicationContext, errorMsg, Toast.LENGTH_LONG).show()
                } finally {
                    btnLogin.isEnabled = true
                    btnLogin.text = "Login"
                }
            }
        }

        tvGoToSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}