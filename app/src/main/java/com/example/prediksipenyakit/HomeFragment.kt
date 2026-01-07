package com.example.prediksipenyakit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
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
    private lateinit var tvGreeting: TextView
    private lateinit var btnStartCheck: Button

    // Variabel Navigasi (Tombol di dalam Fragment)
    private lateinit var imgProfileTop: ImageView
    private lateinit var tvSeeAll: TextView

    // Variabel Statistik
    private lateinit var tvBMIValue: TextView
    private lateinit var tvStepsValue: TextView
    private lateinit var tvSleepValue: TextView
    private lateinit var tvWaterValue: TextView

    private lateinit var rvArticles: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. INISIALISASI VIEW (Sesuai ID di XML fragment_home)
        tvGreeting = view.findViewById(R.id.tvGreeting)
        btnStartCheck = view.findViewById(R.id.btnStartCheck)

        // Init Tombol Navigasi
        imgProfileTop = view.findViewById(R.id.imgProfile) // Foto profil di pojok atas
        tvSeeAll = view.findViewById(R.id.tvSeeAll)         // Teks "Lihat Semua"

        tvBMIValue = view.findViewById(R.id.tvBMIValue)
        tvStepsValue = view.findViewById(R.id.tvStepsValue)
        tvSleepValue = view.findViewById(R.id.tvSleepValue)
        tvWaterValue = view.findViewById(R.id.tvWaterValue)

        rvArticles = view.findViewById(R.id.recyclerViewArticles)

        // 2. LOAD DATA DARI SUPABASE
        loadUserProfile()
        loadLastPredictionStats()
        loadArticles()

        // 3. EVENT LISTENER

        // A. Klik Tombol "Mulai Cek"
        btnStartCheck.setOnClickListener {
            val intent = Intent(requireContext(), PredictionActivity::class.java)
            startActivity(intent)
        }

        // B. Klik Foto Profil (Atas) -> Pindah ke Tab Profil (Bawah)
        imgProfileTop.setOnClickListener {
            if (activity is HomeActivity) {
                (activity as HomeActivity).bukaTabProfil()
            }
        }

        // C. Klik "Lihat Semua" -> Pindah ke Tab Berita (Bawah)
        tvSeeAll.setOnClickListener {
            if (activity is HomeActivity) {
                (activity as HomeActivity).bukaTabBerita()
            }
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

    // --- FUNGSI 2: AMBIL DATA STATISTIK TERAKHIR ---
    private fun loadLastPredictionStats() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val user = SupabaseClient.client.auth.currentUserOrNull()
                val userId = user?.id

                if (userId != null) {
                    val result = SupabaseClient.client.from("prediction_history")
                        .select {
                            filter { eq("user_id", userId) }
                            order("created_at", Order.DESCENDING)
                            limit(1)
                        }.decodeSingleOrNull<PredictionHistoryModel>()

                    withContext(Dispatchers.Main) {
                        if (result != null) {
                            tvStepsValue.text = "${result.dailySteps}"
                            tvSleepValue.text = "${String.format("%.1f", result.sleepHours)}h"
                            tvWaterValue.text = "${String.format("%.1f", result.waterIntake)}L"

                            val bmi = result.bmi
                            val bmiFormatted = String.format("%.1f", bmi)
                            val category = when {
                                bmi < 18.5 -> "Kurus"
                                bmi < 24.9 -> "Normal"
                                bmi < 29.9 -> "Gemuk"
                                else -> "Obesitas"
                            }
                            tvBMIValue.text = "$bmiFormatted ($category)"
                        } else {
                            tvStepsValue.text = "-"
                            tvSleepValue.text = "-"
                            tvWaterValue.text = "-"
                            tvBMIValue.text = "-"
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error stats: ${e.message}")
                withContext(Dispatchers.Main) {
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
                    .select {
                        limit(5)
                        order("published_at", Order.DESCENDING)
                    }
                    .decodeList<ArticleModel>()

                withContext(Dispatchers.Main) {
                    if (articles.isNotEmpty()) {
                        setupRecyclerView(articles)
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeFragment", "Gagal ambil artikel: ${e.message}")
            }
        }
    }

    // --- FUNGSI 4: SETUP RECYCLERVIEW ---
    private fun setupRecyclerView(data: List<ArticleModel>) {
        val adapter = HomeArticleAdapter(data) { articleClicked ->
            // Saat Artikel diklik -> Buka Detail News
            val detailFragment = DetailNewsFragment()
            val bundle = Bundle().apply {
                putString("TITLE", articleClicked.title)
                putString("CONTENT", articleClicked.content)
                putString("CATEGORY", articleClicked.category)
                putString("DATE", articleClicked.publishedAt ?: "")
                putString("IMAGE_URL", articleClicked.imageUrl)
            }
            detailFragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.fade_in, android.R.anim.fade_out,
                    android.R.anim.fade_in, android.R.anim.fade_out
                )
                .replace(R.id.fragmentContainer, detailFragment)
                .addToBackStack(null)
                .commit()
        }

        rvArticles.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvArticles.adapter = adapter
    }
}