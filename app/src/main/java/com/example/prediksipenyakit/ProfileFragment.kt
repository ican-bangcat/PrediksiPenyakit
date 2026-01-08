package com.example.prediksipenyakit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class ProfileFragment : Fragment() {

    private lateinit var etFullNameView: TextInputEditText
    private lateinit var etEmailView: TextInputEditText
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnLogout: MaterialButton
    private lateinit var progressBar: ProgressBar

    // Variabel untuk menyimpan data sementara agar bisa dikirim ke halaman Edit
    private var currentFirstName: String = ""
    private var currentLastName: String = ""
    private var currentEmail: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Gunakan layout baru yang hanya untuk VIEW
        return inflater.inflate(R.layout.fragment_profile_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inisialisasi View
        etFullNameView = view.findViewById(R.id.etFullNameView)
        etEmailView = view.findViewById(R.id.etEmailView)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)
        btnLogout = view.findViewById(R.id.btnLogout)
        progressBar = view.findViewById(R.id.progressBar)

        // 2. Load Data Profil Saat Ini
        loadUserProfile()

        // 3. Listener Tombol Edit Profil
        btnEditProfile.setOnClickListener {
            // Pindah ke EditProfileFragment dan kirim data saat ini
            val editFragment = EditProfileFragment().apply {
                arguments = Bundle().apply {
                    putString("FIRST_NAME", currentFirstName)
                    putString("LAST_NAME", currentLastName)
                    putString("EMAIL", currentEmail)
                }
            }

            // Ganti fragment saat ini dengan EditProfileFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, editFragment) // Pastikan ID container di HomeActivity benar
                .addToBackStack(null) // Agar bisa di-back
                .commit()
        }

        // 4. Listener Tombol Logout
        btnLogout.setOnClickListener {
            performLogout()
        }
    }

    private fun loadUserProfile() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                val userId = currentUser?.id
                currentEmail = currentUser?.email ?: ""

                if (userId != null) {
                    // Ambil Data Profile dari tabel 'profiles'
                    val profileData = SupabaseClient.client.from("profiles")
                        .select {
                            filter { eq("id", userId) }
                        }.decodeSingleOrNull<UserProfileData>()

                    withContext(Dispatchers.Main) {
                        etEmailView.setText(currentEmail)

                        if (profileData != null) {
                            currentFirstName = profileData.firstName ?: ""
                            currentLastName = profileData.lastName ?: ""
                            val gabungan = "$currentFirstName $currentLastName".trim()
                            etFullNameView.setText(if (gabungan.isNotEmpty()) gabungan else "Belum diatur")
                        }
                        progressBar.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                Log.e("PROFILE", "Error loading: ${e.message}")
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Gagal memuat profil", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun performLogout() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                SupabaseClient.client.auth.signOut()
                withContext(Dispatchers.Main) {
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    activity?.finish()
                }
            } catch (e: Exception) {
                Log.e("LOGOUT", "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Gagal Logout", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Data Class untuk Parsing JSON Supabase
    @kotlin.OptIn(kotlinx.serialization.InternalSerializationApi::class) // <--- TAMBAHKAN BARIS INI
    @kotlinx.serialization.Serializable
    data class UserProfileData(
        val id: String,
        @kotlinx.serialization.SerialName("first_name") val firstName: String? = null,
        @kotlinx.serialization.SerialName("last_name") val lastName: String? = null,
        val role: String? = "user"
    )
}