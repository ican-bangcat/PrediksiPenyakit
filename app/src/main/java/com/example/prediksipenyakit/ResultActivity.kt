package com.example.prediksipenyakit

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prediksipenyakit.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private var userInput: UserInputModel? = null
    private var isAtRisk: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userInput = intent.getParcelableExtra("USER_INPUT")
        isAtRisk = intent.getBooleanExtra("IS_AT_RISK", false)

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        userInput?.let { data ->
            // Setup Header Status
            if (isAtRisk) {
                binding.tvStatusTitle.text = "BERISIKO TINGGI"
                binding.tvStatusTitle.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
                binding.cardStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.risk_high_bg)) // Pastikan warna ini ada atau ganti #FFEBEE
                binding.iconStatus.setImageResource(R.drawable.ic_warning) // Pastikan icon ada
                binding.tvStatusDescription.text = "Waspada! Parameter kesehatan Anda menunjukkan tanda bahaya. Segera lakukan perbaikan gaya hidup."
            } else {
                binding.tvStatusTitle.text = "KONDISI PRIMA"
                binding.tvStatusTitle.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
                binding.cardStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.risk_low_bg)) // Pastikan warna ini ada atau ganti #E8F5E9
                binding.iconStatus.setImageResource(R.drawable.ic_check_circle)
                binding.tvStatusDescription.text = "Kerja bagus! Tubuh Anda dalam kondisi sehat. Pertahankan kebiasaan baik ini."
            }

            // Generate saran dengan kalimat PERINTAH & BOLD
            val adviceList = generateHealthAdvice(data)

            if (adviceList.isNotEmpty()) {
                val adapter = HealthAdviceAdapter(adviceList)
                binding.recyclerAdvice.layoutManager = LinearLayoutManager(this)
                binding.recyclerAdvice.adapter = adapter
            }
        }
    }

    private fun generateHealthAdvice(data: UserInputModel): List<String> {
        val adviceList = mutableListOf<String>()

        // Gunakan tag <b> untuk Bold. Nanti di Adapter pakai Html.fromHtml()

        // 1. BMI
        when {
            data.bmi < 18.5 -> adviceList.add("<b>Tingkatkan asupan nutrisi!</b> Berat badan Anda kurang (Underweight). Makanlah lebih sering dengan porsi bergizi.")
            data.bmi >= 25.0 -> adviceList.add("<b>Turunkan berat badan segera!</b> BMI Anda berlebih. Kurangi gula, gorengan, dan perbanyak gerak.")
        }

        // 2. Langkah
        if (data.dailySteps < 5000) {
            adviceList.add("<b>Jangan malas bergerak!</b> Anda hanya melangkah ${data.dailySteps}x. Targetkan minimal 7.000 langkah mulai besok.")
        }

        // 3. Tidur
        if (data.sleepHours < 6) {
            adviceList.add("<b>Tidur lebih awal!</b> Istirahat Anda kurang (${data.sleepHours} jam). Kurang tidur memicu penyakit jantung.")
        }

        // 4. Rokok
        if (data.smoker == 1) {
            adviceList.add("<b>BERHENTI MEROKOK SEKARANG!</b> Ini adalah faktor risiko terbesar. Paru-paru dan jantung Anda dalam bahaya.")
        }

        // 5. Air
        if (data.waterIntake < 2.0) {
            adviceList.add("<b>Minum air putih sekarang!</b> Tubuh Anda butuh minimal 2 Liter air. Jangan tunggu sampai haus.")
        }

        // 6. Tensi
        if (data.systolicBp >= 140 || data.diastolicBp >= 90) {
            adviceList.add("<b>Waspada Hipertensi!</b> Tekanan darah tinggi (${data.systolicBp}/${data.diastolicBp}). Kurangi garam total dan periksa ke dokter.")
        }

        // Jika Sehat
        if (adviceList.isEmpty() && !isAtRisk) {
            adviceList.add("<b>Pertahankan performa ini!</b> Pola hidup Anda sudah sangat baik. Lanjutkan olahraga rutin.")
        }

        return adviceList
    }

    private fun setupClickListeners() {
        binding.btnCheckAgain.setOnClickListener {
            finish() // Kembali ke form
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        // Tombol Baru: Pindah ke ReportActivity
        binding.btnViewReport.setOnClickListener {
            val intent = Intent(this, ReportActivity::class.java)
            // Kirim data user ke halaman report
            intent.putExtra("USER_DATA", userInput)
            startActivity(intent)
        }
    }
}