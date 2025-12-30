package com.example.prediksipenyakit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inisialisasi View
        val tvGreeting = view.findViewById<TextView>(R.id.tvGreeting)
        val btnStartCheck = view.findViewById<Button>(R.id.btnStartCheck)

        // 2. Load Nama User dari Supabase
        loadUserProfile(tvGreeting)

        // 3. Tombol Mulai Cek
        btnStartCheck.setOnClickListener {
            val intent = Intent(requireContext(), PredictionActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserProfile(tvGreeting: TextView) {
        // Jalankan di Background Thread agar UI tidak macet
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // A. Ambil User ID dari Auth
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                val userId = currentUser?.id

                if (userId != null) {
                    // B. Query ke Tabel 'profiles' berdasarkan ID
                    val result = SupabaseClient.client.from("profiles")
                        .select {
                            filter {
                                eq("id", userId)
                            }
                        }.decodeSingleOrNull<ProfileModel>()

                    // C. Update UI di Main Thread
                    withContext(Dispatchers.Main) {
                        if (result != null) {
                            // Tampilkan Nama Depan
                            tvGreeting.text = "Hi, ${result.firstName}"
                        } else {
                            // Fallback jika profil belum ada
                            tvGreeting.text = "Hi, User"
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HOME_PROFILE", "Gagal ambil profil: ${e.message}")
                withContext(Dispatchers.Main) {
                    tvGreeting.text = "Hi, User"
                }
            }
        }
    }
}