package com.example.prediksipenyakit

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileFragment : Fragment() {

    private lateinit var etFirstName: TextInputEditText
    private lateinit var etLastName: TextInputEditText
    private lateinit var etEmailEdit: TextInputEditText
    private lateinit var etPasswordEdit: TextInputEditText
    private lateinit var btnSaveChanges: MaterialButton
    private lateinit var btnClose: ImageView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Init Views
        etFirstName = view.findViewById(R.id.etFirstName)
        etLastName = view.findViewById(R.id.etLastName)
        etEmailEdit = view.findViewById(R.id.etEmailEdit)
        etPasswordEdit = view.findViewById(R.id.etPasswordEdit)
        btnSaveChanges = view.findViewById(R.id.btnSaveChanges)
        btnClose = view.findViewById(R.id.btnClose)
        progressBar = view.findViewById(R.id.progressBarEdit)

        // 2. Ambil data yang dikirim dari ProfileFragment (biar gak ngetik ulang)
        val argFirstName = arguments?.getString("FIRST_NAME") ?: ""
        val argLastName = arguments?.getString("LAST_NAME") ?: ""
        val argEmail = arguments?.getString("EMAIL") ?: ""

        // 3. Set Text Awal
        etFirstName.setText(argFirstName)
        etLastName.setText(argLastName)
        etEmailEdit.setText(argEmail)

        // 4. Tombol Close (Kembali tanpa simpan)
        btnClose.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 5. Tombol Simpan
        btnSaveChanges.setOnClickListener {
            val fName = etFirstName.text.toString().trim()
            val lName = etLastName.text.toString().trim()
            val email = etEmailEdit.text.toString().trim()
            val password = etPasswordEdit.text.toString().trim()

            if (fName.isEmpty()) {
                etFirstName.error = "Wajib diisi"
                return@setOnClickListener
            }

            saveProfileChanges(fName, lName, email, password)
        }
    }

    private fun saveProfileChanges(fName: String, lName: String, email: String, password: String) {
        progressBar.visibility = View.VISIBLE
        btnSaveChanges.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                if (currentUser == null) return@launch

                // A. Update Auth (Email & Password) jika ada perubahan
                // Cek apakah email berubah atau password diisi
                if ((email.isNotEmpty() && email != currentUser.email) || password.isNotEmpty()) {
                    SupabaseClient.client.auth.updateUser {
                        if (email.isNotEmpty() && email != currentUser.email) this.email = email
                        if (password.isNotEmpty()) this.password = password
                    }
                }

                // B. Update Data Profile (First Name & Last Name) ke Database
                val updateData = UserProfileUpdate(
                    id = currentUser.id,
                    firstName = fName,
                    lastName = lName
                )

                // Gunakan upsert: Update jika ada, Insert jika belum
                SupabaseClient.client.from("profiles").upsert(updateData)

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnSaveChanges.isEnabled = true
                    Toast.makeText(requireContext(), "Profil Berhasil Diupdate!", Toast.LENGTH_SHORT).show()

                    // Kembali ke halaman Profile View
                    parentFragmentManager.popBackStack()
                }

            } catch (e: Exception) {
                Log.e("EDIT_PROFILE", "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    btnSaveChanges.isEnabled = true
                    Toast.makeText(requireContext(), "Gagal update: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @kotlinx.serialization.Serializable
    data class UserProfileUpdate(
        val id: String,
        @kotlinx.serialization.SerialName("first_name") val firstName: String,
        @kotlinx.serialization.SerialName("last_name") val lastName: String
    )
}