package com.example.prediksipenyakit

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Inisialisasi View
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnSave = findViewById<MaterialButton>(R.id.btnSave)

        val etFullName = findViewById<TextInputEditText>(R.id.etFullName)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)

        // Contoh: Set data dummy (bisa diganti data dari database/intent)
        etFullName.setText("Pasien #8021")
        etEmail.setText("pasien@example.com")

        // Listener Tombol Back
        btnBack.setOnClickListener {
            finish() // Kembali ke halaman sebelumnya (Home)
        }

        // Listener Tombol Simpan
        btnSave.setOnClickListener {
            // Validasi Sederhana
            val name = etFullName.text.toString()
            if (name.isEmpty()) {
                etFullName.error = "Nama tidak boleh kosong"
                return@setOnClickListener
            }

            // Simpan Data (Simulasi)
            Toast.makeText(this, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()

            // Opsional: Kembali ke home setelah simpan
            // finish()
        }
    }
}