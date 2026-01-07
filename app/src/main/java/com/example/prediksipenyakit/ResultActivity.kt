package com.example.prediksipenyakit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prediksipenyakit.databinding.ActivityResultBinding
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private var userInput: UserInputModel? = null
    private var isAtRisk: Boolean = false

    // Adapter Artikel Disimpan di sini
    private lateinit var articleAdapter: ResultArticleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil Data dari Intent
        userInput = intent.getParcelableExtra("USER_INPUT")
        isAtRisk = intent.getBooleanExtra("IS_AT_RISK", false)

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        userInput?.let { data ->
            // --- 1. SETUP STATUS HEADER ---
            if (isAtRisk) {
                binding.tvStatusTitle.text = "BERISIKO TINGGI"
                binding.tvStatusTitle.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
                binding.cardStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.risk_high_bg)) // Pastikan warna ada di colors.xml atau ganti hex code
                binding.iconStatus.setImageResource(R.drawable.ic_warning)
                binding.tvStatusDescription.text = "Waspada! Parameter kesehatan Anda menunjukkan tanda bahaya."
            } else {
                binding.tvStatusTitle.text = "KONDISI PRIMA"
                binding.tvStatusTitle.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
                binding.cardStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.risk_low_bg))
                binding.iconStatus.setImageResource(R.drawable.ic_check_circle)
                binding.tvStatusDescription.text = "Kerja bagus! Tubuh Anda dalam kondisi sehat."
            }

            // 2. SETUP REKOMENDASI DOKTER (INI YANG HILANG TADI)
            val adviceList = generateHealthAdvice(data)

            // Setup RecyclerView Rekomendasi Dokter
            val doctorAdapter = HealthAdviceAdapter(adviceList)
            binding.recyclerAdvice.layoutManager = LinearLayoutManager(this)
            binding.recyclerAdvice.adapter = doctorAdapter

            // 3. SETUP ARTIKEL DARI DATABAS---
            setupArticleRecyclerView()
            fetchArticlesFromSupabase(data)
        }
    }

    // LOGIKA REKOMENDASI DOKTER (WAJIB ADA)---
    private fun generateHealthAdvice(data: UserInputModel): MutableList<String> {
        val adviceList = mutableListOf<String>()

        // 1. BMI
        when {
            data.bmi < 18.5 -> adviceList.add("<b>Tingkatkan asupan nutrisi!</b> Berat badan Anda kurang. Makanlah lebih sering dengan porsi bergizi.")
            data.bmi >= 25.0 -> adviceList.add("<b>Turunkan berat badan!</b> BMI Anda berlebih. Kurangi gula, gorengan, dan perbanyak gerak.")
        }

        // 2. Langkah
        if (data.dailySteps < 5000) {
            adviceList.add("<b>Jangan malas bergerak!</b> Anda hanya melangkah ${data.dailySteps}x. Targetkan minimal 7.000 langkah.")
        }

        // 3. Tidur
        if (data.sleepHours < 6) {
            adviceList.add("<b>Tidur lebih awal!</b> Istirahat Anda kurang (${data.sleepHours} jam). Kurang tidur memicu penyakit jantung.")
        }

        // 4. Rokok
        if (data.smoker == 1) {
            adviceList.add("<b>BERHENTI MEROKOK SEKARANG!</b> Ini faktor risiko terbesar. Paru-paru dan jantung Anda dalam bahaya.")
        }

        // 5. Air
        if (data.waterIntake < 2.0) {
            adviceList.add("<b>Minum air putih sekarang!</b> Tubuh butuh minimal 2 Liter air. Jangan tunggu haus.")
        }

        // 6. Tensi
        val sys = data.systolicBp
        val dia = data.diastolicBp

        if (sys >= 160 || dia >= 100) {
            // Hipertensi Tingkat 2 (Bahaya)
            adviceList.add("<b>BAHAYA: Hipertensi Tingkat 2!</b> ($sys/$dia). Segera periksa ke dokter/IGD.")
        } else if (sys >= 140 || dia >= 90) {
            // Hipertensi Tingkat 1 (Sesuai code Anda)
            adviceList.add("<b>Waspada Hipertensi!</b> Tensi Anda tinggi ($sys/$dia). Kurangi garam segera.")
        } else if (sys >= 120 || dia >= 80) {
            // Pre-Hipertensi (Penting untuk pencegahan)
            adviceList.add("<b>Hati-hati: Pre-Hipertensi.</b> Tensi sedikit di atas normal ($sys/$dia). Jaga pola makan.")
        } else {
            // Normal (< 120 DAN < 80)
            adviceList.add("<b>Tensi Normal.</b> Bagus ($sys/$dia), pertahankan!")
        }

        // 7. JIKA SEHAT (List Kosong) -> Tampilkan Pesan Pujian
        if (adviceList.isEmpty() && !isAtRisk) {
            adviceList.add("<b>Pertahankan performa ini!</b> Pola hidup Anda sudah sangat baik. Lanjutkan olahraga rutin dan makan sehat.")
        } else if (adviceList.isEmpty()) {
            // Jaga-jaga kalau berisiko tapi tidak masuk kriteria di atas
            adviceList.add("<b>Periksa ke Dokter.</b> Hasil prediksi menunjukkan risiko, segera konsultasi lebih lanjut.")
        }

        return adviceList
    }

    // LOGIKA ARTIKEL REKOMENDASI
    private fun setupArticleRecyclerView() {
        articleAdapter = ResultArticleAdapter(emptyList()) { article ->
            // Saat Artikel Diklik -> Buka Detail
            val intent = Intent(this, NewsDetailHostActivity::class.java).apply {
                putExtra("TITLE", article.title)
                putExtra("CONTENT", article.content)
                putExtra("CATEGORY", article.category)
                putExtra("DATE", article.publishedAt)
                putExtra("IMAGE_URL", article.imageUrl)
            }
            startActivity(intent)
        }

        binding.recyclerViewArticles.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewArticles.adapter = articleAdapter
    }

    private fun fetchArticlesFromSupabase(data: UserInputModel) {
        val targetCategories = mutableListOf<String>()

        // Tentukan Kategori Berdasarkan Input User
        if (data.dailySteps < 5000) targetCategories.add("Aktivitas Fisik")
        if (data.sleepHours < 6) targetCategories.add("Tidur & Istirahat")
        if (data.bmi >= 25 || data.bmi < 18.5) targetCategories.add("Nutrisi & Diet")
        if (data.waterIntake < 2.0) targetCategories.add("Nutrisi & Diet")
        if (data.systolicBp >= 140 || data.diastolicBp >= 90) targetCategories.add("Kesehatan Jantung")
        if (data.smoker == 1 || data.alcohol == 1) targetCategories.add("Gaya Hidup")

        // Default categories
        if (targetCategories.isEmpty()) {
            targetCategories.add("Info Medis")
            targetCategories.add("Gaya Hidup")
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Query "WHERE category IN (...)"
                val result = SupabaseClient.client.from("articles")
                    .select {
                        filter {
                            isIn("category", targetCategories)
                        }
                        limit(5)
                    }.decodeList<ArticleModel>()

                withContext(Dispatchers.Main) {
                    if (result.isNotEmpty()) {
                        articleAdapter.updateData(result)
                        binding.recyclerViewArticles.visibility = View.VISIBLE
                        binding.tvArticleTitle.visibility = View.VISIBLE
                        binding.tvSeeAll.visibility = View.VISIBLE
                    } else {
                        binding.recyclerViewArticles.visibility = View.GONE
                        binding.tvArticleTitle.visibility = View.GONE
                        binding.tvSeeAll.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                Log.e("ResultActivity", "Error fetch articles: ${e.message}")
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnCheckAgain.setOnClickListener { finish() }
        binding.btnBack.setOnClickListener { finish() }

        binding.btnViewReport.setOnClickListener {
            val intent = Intent(this, ReportActivity::class.java)
            intent.putExtra("USER_DATA", userInput)
            startActivity(intent)
        }
    }
}