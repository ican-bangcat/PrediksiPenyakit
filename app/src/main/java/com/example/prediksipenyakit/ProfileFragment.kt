package com.example.prediksipenyakit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Hubungkan dengan layout fragment_profile
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inisialisasi View menggunakan 'view.findViewById'
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSave)
        val etFullName = view.findViewById<TextInputEditText>(R.id.etFullName)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)

        // Contoh: Set data dummy
        etFullName.setText("Pasien #8021")
        etEmail.setText("pasien@example.com")

        // 2. Listener Tombol Back
        btnBack.setOnClickListener {
            // Karena ini Fragment, kita panggil fungsi di Activity untuk kembali ke Home
            // Atau cukup panggil tombol home secara programmatically
            (activity as? HomeActivity)?.let { homeActivity ->
                // Memuat ulang HomeFragment
                homeActivity.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, HomeFragment())
                    .commit()

                // PENTING: Update juga warna tombol di bawah agar kembali ke Home (Biru)
                // (Pastikan fungsi updateTabUI di HomeActivity bersifat public jika ingin diakses dari sini,
                //  atau biarkan manual handling)
            }
        }

        // 3. Listener Tombol Simpan
        btnSave.setOnClickListener {
            // Validasi Sederhana
            val name = etFullName.text.toString()
            if (name.isEmpty()) {
                etFullName.error = "Nama tidak boleh kosong"
                return@setOnClickListener
            }

            // Gunakan requireContext() untuk Toast di Fragment
            Toast.makeText(requireContext(), "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
        }
    }
}