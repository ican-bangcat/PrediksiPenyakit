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
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etFirstName = findViewById<TextInputEditText>(R.id.etFirstName)
        val etLastName = findViewById<TextInputEditText>(R.id.etLastName)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmailReg)
        val etPassword = findViewById<TextInputEditText>(R.id.etPasswordReg)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)

        btnSignUp.setOnClickListener {
            val firstName = etFirstName.text.toString()
            val lastName = etLastName.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Mohon isi semua data!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSignUp.isEnabled = false
            btnSignUp.text = "Memproses..."

            lifecycleScope.launch {
                try {
                    // 1. Daftar Akun Baru (Auth)
                    val userSession = SupabaseClient.client.auth.signUpWith(Email) {
                        this.email = email
                        this.password = password
                    }

                    // Ambil User ID yang baru dibuat
                    // Note: Terkadang userSession langsung mengembalikan user, kadang null tergantung settingan 'Confirm Email'
                    // Jika kamu mematikan 'Confirm Email' di Supabase, user otomatis login.
                    val user = SupabaseClient.client.auth.currentUserOrNull()

                    if (user != null) {
                        // 2. Simpan Nama ke Tabel 'profiles'
                        val profileData = ProfileModel(
                            id = user.id,
                            firstName = firstName,
                            lastName = lastName
                        )

                        SupabaseClient.client.from("profiles").insert(profileData)

                        Toast.makeText(applicationContext, "Registrasi Berhasil!", Toast.LENGTH_LONG).show()

                        // Lanjut ke Login atau langsung ke Home (tergantung flow kamu)
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(applicationContext, "Cek email Anda untuk verifikasi!", Toast.LENGTH_LONG).show()
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(applicationContext, "Gagal: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    btnSignUp.isEnabled = true
                    btnSignUp.text = "Daftar Sekarang"
                }
            }
        }

        tvGoToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}