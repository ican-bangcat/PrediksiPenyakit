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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    // --- VARIABEL UI ---
    private lateinit var tvStepsValue: TextView
    private lateinit var tvSleepValue: TextView
    private lateinit var tvWaterValue: TextView
    private lateinit var rvArticles: RecyclerView
    private lateinit var tvGreeting: TextView
    private lateinit var btnStartCheck: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Pastikan nama layout XML fragment kamu benar (misal: fragment_home)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. INISIALISASI VIEW (Sambungkan dengan ID di XML fragment_home)
        tvGreeting = view.findViewById(R.id.tvGreeting)
        btnStartCheck = view.findViewById(R.id.btnStartCheck)

        tvStepsValue = view.findViewById(R.id.tvStepsValue)
        tvSleepValue = view.findViewById(R.id.tvSleepValue)
        tvWaterValue = view.findViewById(R.id.tvWaterValue)
        rvArticles = view.findViewById(R.id.recyclerViewArticles)

        // 2. LOAD DATA DARI SUPABASE
        loadUserProfile()          // Nama User
        loadLastPredictionStats()  // Data Kesehatan Terakhir
        loadArticles()             // Berita/Artikel

        // 3. EVENT LISTENER TOMBOL CEK KESEHATAN
        btnStartCheck.setOnClickListener {
            // Pindah ke Activity Prediksi
            val intent = Intent(requireContext(), PredictionActivity::class.java)
            startActivity(intent)
        }
    }

    // --- FUNGSI 1: AMBIL DATA PROFIL USER ---
    private fun loadUserProfile() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val user = SupabaseClient.client.auth.currentUserOrNull()
                val userId = user?.id

                if (userId != null) {
                    val profile = SupabaseClient.client.from("profiles")
                        .select { filter { eq("id", userId) } }
                        .decodeSingleOrNull<ProfileModel>()

                    withContext(Dispatchers.Main) {
                        tvGreeting.text = "Hi, ${profile?.firstName ?: "User"}"
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error loading profile: ${e.message}")
            }
        }
    }

    // --- FUNGSI 2: AMBIL DATA STATISTIK TERAKHIR (Step, Sleep, Water) ---
    private fun loadLastPredictionStats() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val user = SupabaseClient.client.auth.currentUserOrNull()
                val userId = user?.id

                if (userId != null) {
                    // Query: Ambil data milik user ini, urutkan created_at DESC (terbaru), ambil 1
                    val result = SupabaseClient.client.from("predictions")
                        .select {
                            filter { eq("user_id", userId) }
                            order("created_at", Order.DESCENDING)
                            limit(1)
                        }.decodeSingleOrNull<PredictionHistoryModel>()

                    Log.d("HomeFragment", "Data Stats: $result") // Cek di Logcat

                    withContext(Dispatchers.Main) {
                        if (result != null) {
                            // Tampilkan data jika ada
                            tvStepsValue.text = "${result.dailySteps}"
                            tvSleepValue.text = "${result.sleepHours}h"
                            tvWaterValue.text = "${result.waterIntake}L"
                        } else {
                            // Tampilkan strip jika belum pernah cek
                            tvStepsValue.text = "-"
                            tvSleepValue.text = "-"
                            tvWaterValue.text = "-"
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error stats: ${e.message}")
                withContext(Dispatchers.Main) {
                    // Jika error (misal internet mati), tampilkan "-"
                    tvStepsValue.text = "-"
                }
            }
        }
    }

    // --- FUNGSI 3: AMBIL ARTIKEL ---
    private fun loadArticles() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val articles = SupabaseClient.client.from("articles")
                    .select()
                    .decodeList<ArticleModel>()

                withContext(Dispatchers.Main) {
                    if (articles.isNotEmpty()) {
                        setupRecyclerView(articles)
                    } else {
                        setupRecyclerView(getDummyArticles())
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Gagal ambil artikel: ${e.message}")
                withContext(Dispatchers.Main) {
                    setupRecyclerView(getDummyArticles())
                }
            }
        }
    }

    // --- FUNGSI 4: SETUP RECYCLERVIEW & KLIK ARTIKEL ---
    private fun setupRecyclerView(data: List<ArticleModel>) {
        val adapter = HomeArticleAdapter(data) { articleClicked ->

            // --- LOGIKA KLIK ARTIKEL ---
            // 1. Siapkan Fragment Detail
            val detailFragment = DetailNewsFragment()

            // 2. Bungkus data ke Bundle
            val bundle = Bundle().apply {
                putString("TITLE", articleClicked.title)
                putString("CONTENT", articleClicked.content)
                putString("CATEGORY", articleClicked.category)
                putString("DATE", articleClicked.publishedAt ?: "")
                putString("IMAGE_URL", articleClicked.imageUrl)
            }
            detailFragment.arguments = bundle

            // 3. Transaksi Fragment (Ganti Tampilan)
            // PENTING: Gunakan R.id.fragmentContainer sesuai XML activity_home.xml
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.fade_in, android.R.anim.fade_out,
                    android.R.anim.fade_in, android.R.anim.fade_out
                )
                .replace(R.id.fragmentContainer, detailFragment)
                .addToBackStack(null) // Agar bisa tombol back
                .commit()
        }

        rvArticles.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvArticles.adapter = adapter
    }

    // Data Dummy (Cadangan jika database kosong)
    private fun getDummyArticles(): List<ArticleModel> {
        return listOf(
            ArticleModel(title = "Tips Jantung Sehat", content = "...", category = "Kesehatan"),
            ArticleModel(title = "Manfaat Jalan Kaki", content = "...", category = "Olahraga"),
            ArticleModel(title = "Pentingnya Air Putih", content = "...", category = "Nutrisi")
        )
    }
}