package com.example.prediksipenyakit

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    // --- SETUP SHARED PREFERENCES (REMEMBER ME) ---
    private val PREFS_NAME = "UserLoginPrefs"
    private val KEY_EMAIL = "email"
    private val KEY_PASSWORD = "password"
    private val KEY_REMEMBER = "remember"
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inisialisasi View
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvGoToSignUp = findViewById<TextView>(R.id.tvGoToSignUp)
        val cbRemember = findViewById<CheckBox>(R.id.cbRemember)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword) // Pastikan ID ini ada di XML

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // 1. CEK DATA REMEMBER ME SAAT APLIKASI DIBUKA
        if (sharedPreferences.getBoolean(KEY_REMEMBER, false)) {
            etEmail.setText(sharedPreferences.getString(KEY_EMAIL, ""))
            etPassword.setText(sharedPreferences.getString(KEY_PASSWORD, ""))
            cbRemember.isChecked = true
        }

        // 2. LOGIKA TOMBOL LOGIN
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
                    // A. Login ke Supabase Auth
                    SupabaseClient.client.auth.signInWith(Email) {
                        this.email = email
                        this.password = password
                    }

                    // B. Simpan Remember Me jika dicentang
                    val editor = sharedPreferences.edit()
                    if (cbRemember.isChecked) {
                        editor.putString(KEY_EMAIL, email)
                        editor.putString(KEY_PASSWORD, password)
                        editor.putBoolean(KEY_REMEMBER, true)
                    } else {
                        editor.clear()
                    }
                    editor.apply()

                    // C. Ambil Data User & Profile untuk Cek Role
                    val user = SupabaseClient.client.auth.currentUserOrNull()
                    if (user != null) {
                        val userId = user.id

                        // Query ke tabel 'profiles' menggunakan ProfileModel yang baru
                        val profile = SupabaseClient.client.from("profiles")
                            .select {
                                filter {
                                    eq("id", userId)
                                }
                            }.decodeSingle<ProfileModel>()

                        // D. Logika Role & Notifikasi
                        if (profile.role == "admin") {
                            // --- JIKA ADMIN ---
                            Toast.makeText(applicationContext, "Selamat Datang Admin", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@LoginActivity, AdminActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        } else {
                            // --- JIKA USER BIASA ---
                            // Mengambil firstName dari ProfileModel
                            Toast.makeText(applicationContext, "Selamat Datang User ${profile.firstName}", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    val errorMsg = if (e.message?.contains("Invalid login credentials") == true)
                        "Email atau Password Salah" else "Gagal Login: ${e.message}"
                    Toast.makeText(applicationContext, errorMsg, Toast.LENGTH_LONG).show()
                } finally {
                    btnLogin.isEnabled = true
                    btnLogin.text = "Login"
                }
            }
        }

        // 3. LOGIKA LUPA PASSWORD
        tvForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

        // 4. PINDAH KE REGISTER
        tvGoToSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    // Fungsi Pop-up Lupa Password
    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Reset Password")
        builder.setMessage("Masukkan email Anda untuk menerima link reset password.")

        val input = EditText(this)
        input.hint = "Email Address"
        input.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        builder.setView(input)

        builder.setPositiveButton("Kirim") { _, _ ->
            val emailReset = input.text.toString()
            if (emailReset.isNotEmpty()) {
                performResetPassword(emailReset)
            } else {
                Toast.makeText(this, "Email tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Batal") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    // Fungsi Kirim Email Reset ke Supabase
    private fun performResetPassword(email: String) {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.auth.resetPasswordForEmail(email)
                Toast.makeText(applicationContext, "Link reset password telah dikirim ke email Anda", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(applicationContext, "Gagal mengirim email: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}