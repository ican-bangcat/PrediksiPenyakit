package com.example.prediksipenyakit

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prediksipenyakit.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private var userInput: UserInputModel? = null
    private var isAtRisk: Boolean = false
    private var riskProbability: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ambil data dari Intent
        userInput = intent.getParcelableExtra("USER_INPUT")
        isAtRisk = intent.getBooleanExtra("IS_AT_RISK", false)
        riskProbability = intent.getFloatExtra("RISK_PROBABILITY", 0f)

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        userInput?.let { data ->
            // Setup Header Status
            if (isAtRisk) {
                binding.tvStatusTitle.text = "Beresiko Tinggi"
                binding.tvStatusTitle.setTextColor(ContextCompat.getColor(this, R.color.risk_high))
                binding.cardStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.risk_high_bg))
                binding.iconStatus.setImageResource(R.drawable.ic_warning)
                binding.tvStatusDescription.text = "Hasil analisis menunjukkan Anda memiliki risiko tinggi terkena penyakit. Harap perhatikan saran di bawah ini."
            } else {
                binding.tvStatusTitle.text = "Sehat"
                binding.tvStatusTitle.setTextColor(ContextCompat.getColor(this, R.color.risk_low))
                binding.cardStatus.setCardBackgroundColor(ContextCompat.getColor(this, R.color.risk_low_bg))
                binding.iconStatus.setImageResource(R.drawable.ic_check_circle)
                binding.tvStatusDescription.text = "Selamat! Kondisi kesehatan Anda dalam kategori baik. Tetap jaga pola hidup sehat."
            }

            // Tampilkan probabilitas
            binding.tvProbability.text = "Probabilitas Risiko: ${String.format("%.1f", riskProbability * 100)}%"

            // Generate saran kesehatan
            val adviceList = generateHealthAdvice(data)

            if (adviceList.isNotEmpty()) {
                binding.tvAdviceTitle.visibility = View.VISIBLE
                binding.recyclerAdvice.visibility = View.VISIBLE

                val adapter = HealthAdviceAdapter(adviceList)
                binding.recyclerAdvice.layoutManager = LinearLayoutManager(this)
                binding.recyclerAdvice.adapter = adapter
            } else {
                binding.tvAdviceTitle.visibility = View.GONE
                binding.recyclerAdvice.visibility = View.GONE
            }

            // Tampilkan ringkasan data
            displayDataSummary(data)
        }
    }

    private fun generateHealthAdvice(data: UserInputModel): List<String> {
        val adviceList = mutableListOf<String>()

        // Analisis BMI
        when {
            data.bmi < 18.5 -> adviceList.add("BMI Anda ${String.format("%.1f", data.bmi)} termasuk underweight. Tingkatkan asupan nutrisi dan konsultasikan dengan ahli gizi.")
            data.bmi in 25.0..29.9 -> adviceList.add("BMI Anda ${String.format("%.1f", data.bmi)} termasuk overweight. Kurangi kalori dan perbanyak aktivitas fisik untuk mencapai berat badan ideal.")
            data.bmi >= 30.0 -> adviceList.add("BMI Anda ${String.format("%.1f", data.bmi)} termasuk obesitas. Segera konsultasikan dengan dokter dan ahli gizi untuk program penurunan berat badan.")
        }

        // Analisis Aktivitas Fisik
        if (data.dailySteps < 5000) {
            adviceList.add("Langkah harian Anda hanya ${data.dailySteps} langkah. Tingkatkan aktivitas fisik minimal 7.000-10.000 langkah per hari untuk kesehatan jantung.")
        }

        // Analisis Tidur
        when {
            data.sleepHours < 6 -> adviceList.add("Durasi tidur Anda ${String.format("%.1f", data.sleepHours)} jam terlalu singkat. Tidur 7-9 jam per hari penting untuk pemulihan tubuh dan mencegah penyakit.")
            data.sleepHours > 9 -> adviceList.add("Durasi tidur Anda ${String.format("%.1f", data.sleepHours)} jam terlalu lama. Tidur berlebihan dapat mengindikasikan masalah kesehatan, konsultasikan dengan dokter.")
        }

        // Analisis Merokok
        if (data.smoker == 1) {
            adviceList.add("Anda adalah perokok aktif. Merokok meningkatkan risiko penyakit jantung, kanker, dan stroke secara signifikan. Berhentilah merokok sekarang juga!")
        }

        // Analisis Alkohol
        if (data.alcohol == 1) {
            adviceList.add("Konsumsi alkohol dapat meningkatkan risiko penyakit hati, jantung, dan kanker. Batasi atau hentikan konsumsi alkohol untuk kesehatan jangka panjang.")
        }

        // Analisis Asupan Air
        if (data.waterIntake < 2.0) {
            adviceList.add("Asupan air Anda ${String.format("%.1f", data.waterIntake)} liter kurang dari rekomendasi. Minum minimal 2-3 liter air per hari untuk hidrasi optimal.")
        }

        // Analisis Kalori
        val idealCalories = when (data.gender.lowercase()) {
            "male" -> if (data.age < 50) 2500 else 2200
            "female" -> if (data.age < 50) 2000 else 1800
            else -> 2000
        }

        if (data.caloriesConsumed > idealCalories + 500) {
            adviceList.add("Asupan kalori Anda ${data.caloriesConsumed} kkal terlalu tinggi. Kurangi kalori dan pilih makanan bergizi seimbang.")
        } else if (data.caloriesConsumed < idealCalories - 500) {
            adviceList.add("Asupan kalori Anda ${data.caloriesConsumed} kkal terlalu rendah. Pastikan nutrisi tercukupi untuk metabolisme optimal.")
        }

        // Analisis Tekanan Darah
        when {
            data.systolicBp >= 140 || data.diastolicBp >= 90 -> {
                adviceList.add("Tekanan darah Anda ${data.systolicBp}/${data.diastolicBp} mmHg termasuk hipertensi. Segera konsultasi dokter, kurangi garam, dan kelola stres.")
            }
            data.systolicBp in 120..139 || data.diastolicBp in 80..89 -> {
                adviceList.add("Tekanan darah Anda ${data.systolicBp}/${data.diastolicBp} mmHg termasuk prehipertensi. Jaga pola makan rendah garam dan rutin berolahraga.")
            }
            data.systolicBp < 90 || data.diastolicBp < 60 -> {
                adviceList.add("Tekanan darah Anda ${data.systolicBp}/${data.diastolicBp} mmHg terlalu rendah. Konsultasikan dengan dokter jika sering merasa pusing atau lemas.")
            }
        }

        // Analisis Heart Rate
        val restingHeartRate = data.heartRate
        when {
            restingHeartRate > 100 -> adviceList.add("Detak jantung istirahat Anda ${restingHeartRate} bpm terlalu tinggi. Kelola stres, hindari kafein berlebih, dan konsultasi dokter.")
            restingHeartRate < 60 && !isAthlete(data) -> adviceList.add("Detak jantung istirahat Anda ${restingHeartRate} bpm terlalu rendah. Jika disertai gejala seperti pusing, segera konsultasi dokter.")
        }

        // Analisis Family History (BARU)
        if (data.familyHistory == 1) {
            adviceList.add("Anda memiliki riwayat penyakit keluarga. Lakukan pemeriksaan kesehatan rutin dan konsultasi dengan dokter untuk pencegahan dini.")
        }

        // Jika tidak ada masalah dan tidak berisiko
        if (adviceList.isEmpty() && !isAtRisk) {
            adviceList.add("Pertahankan pola hidup sehat Anda! Tetap aktif, makan bergizi, dan cukup istirahat.")
            adviceList.add("Lakukan medical check-up rutin setiap 6-12 bulan untuk deteksi dini masalah kesehatan.")
        }

        return adviceList
    }

    private fun isAthlete(data: UserInputModel): Boolean {
        // Asumsi sederhana: atlet jika langkah harian > 15000 dan BMI normal
        return data.dailySteps > 15000 && data.bmi in 18.5..24.9
    }

    private fun displayDataSummary(data: UserInputModel) {
        binding.tvDataSummary.text = buildString {
            append("Ringkasan Data Anda:\n\n")
            append("Usia: ${data.age} tahun\n")
            append("Jenis Kelamin: ${data.gender}\n")
            append("BMI: ${String.format("%.1f", data.bmi)}\n")
            append("Langkah Harian: ${data.dailySteps} langkah\n")
            append("Jam Tidur: ${String.format("%.1f", data.sleepHours)} jam\n")
            append("Merokok: ${if (data.smoker == 1) "Ya" else "Tidak"}\n")
            append("Alkohol: ${if (data.alcohol == 1) "Ya" else "Tidak"}\n")
            append("Asupan Air: ${String.format("%.1f", data.waterIntake)} L\n")
            append("Kalori: ${data.caloriesConsumed} kkal\n")
            append("Tekanan Darah: ${data.systolicBp}/${data.diastolicBp} mmHg\n")
            append("Detak Jantung: ${data.heartRate} bpm\n")
            append("Riwayat Keluarga: ${if (data.familyHistory == 1) "Ya" else "Tidak"}")
        }
    }

    private fun setupClickListeners() {
        binding.btnCheckAgain.setOnClickListener {
            finish()
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }
}

// Data class untuk menerima input user
data class UserInputModel(
    val age: Int,
    val gender: String,
    val bmi: Float,
    val dailySteps: Int,
    val sleepHours: Float,
    val smoker: Int,
    val alcohol: Int,
    val waterIntake: Float,
    val caloriesConsumed: Int,
    val systolicBp: Int,
    val diastolicBp: Int,
    val heartRate: Int,
    val familyHistory: Int  // GANTI: Cholesterol → Family History
) : android.os.Parcelable {
    constructor(parcel: android.os.Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readFloat(),
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()  // GANTI: Float → Int
    )

    override fun writeToParcel(parcel: android.os.Parcel, flags: Int) {
        parcel.writeInt(age)
        parcel.writeString(gender)
        parcel.writeFloat(bmi)
        parcel.writeInt(dailySteps)
        parcel.writeFloat(sleepHours)
        parcel.writeInt(smoker)
        parcel.writeInt(alcohol)
        parcel.writeFloat(waterIntake)
        parcel.writeInt(caloriesConsumed)
        parcel.writeInt(systolicBp)
        parcel.writeInt(diastolicBp)
        parcel.writeInt(heartRate)
        parcel.writeInt(familyHistory)  // GANTI
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : android.os.Parcelable.Creator<UserInputModel> {
        override fun createFromParcel(parcel: android.os.Parcel): UserInputModel {
            return UserInputModel(parcel)
        }

        override fun newArray(size: Int): Array<UserInputModel?> {
            return arrayOfNulls(size)
        }
    }
}