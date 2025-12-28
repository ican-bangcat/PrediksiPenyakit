package com.example.prediksipenyakit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.RadioGroup
import android.widget.Toast
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.io.File
import java.io.FileOutputStream
import java.nio.FloatBuffer
import java.util.Collections

class PredictionActivity : AppCompatActivity() {

    // --- UI Components ---
    private lateinit var etBMI: TextInputEditText
    private lateinit var etAge: TextInputEditText
    private lateinit var etGender: AutoCompleteTextView
    private lateinit var etDailySteps: TextInputEditText
    private lateinit var etSleepHours: TextInputEditText
    private lateinit var etWaterIntake: TextInputEditText
    private lateinit var etCalories: TextInputEditText
    private lateinit var etSystolic: TextInputEditText
    private lateinit var etDiastolic: TextInputEditText
    private lateinit var etHeartRate: TextInputEditText
    private lateinit var rgSmoker: RadioGroup
    private lateinit var rgAlcohol: RadioGroup
    private lateinit var rgFamilyHistory: RadioGroup
    private lateinit var btnSubmit: MaterialButton
    private lateinit var btnBack: ImageView

    // --- ONNX Components ---
    private lateinit var ortEnv: OrtEnvironment
    private lateinit var session: OrtSession

    companion object {
        // ========================================================================
        // ⚠️ PENTING: GANTI ANGKA INI DENGAN HASIL OUTPUT PYTHON KAMU!
        // ========================================================================
        // Ini hanya untuk 9 Fitur Numerik:
        // [age, bmi, daily_steps, sleep_hours, water_intake, calories, resting_hr, systolic, diastolic]

        private val MEANS = floatArrayOf(
            48.52f,    // 0: age
            29.02f,    // 1: bmi
            10479.8f,  // 2: daily_steps
            6.49f,     // 3: sleep_hours
            2.75f,     // 4: water_intake
            2603.3f,   // 5: calories
            74.45f,    // 6: resting_hr
            134.58f,   // 7: systolic
            89.50f     // 8: diastolic
        )

        private val STDS = floatArrayOf(
            17.88f,    // 0: age
            6.35f,     // 1: bmi
            5483.6f,   // 2: daily_steps
            2.02f,     // 3: sleep_hours
            1.29f,     // 4: water_intake
            807.2f,    // 5: calories
            14.42f,    // 6: resting_hr
            25.95f,    // 7: systolic
            17.34f     // 8: diastolic
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prediction)

        initViews()
        setupGenderDropdown()
        initONNX()

        btnSubmit.setOnClickListener {
            if (validateInputs()) {
                performPrediction()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        etBMI = findViewById(R.id.etBMI)
        etAge = findViewById(R.id.etAge)
        etGender = findViewById(R.id.etGender)
        etDailySteps = findViewById(R.id.etDailySteps)
        etSleepHours = findViewById(R.id.etSleepHours)
        etWaterIntake = findViewById(R.id.etWaterIntake)
        etCalories = findViewById(R.id.etCalories)
        etSystolic = findViewById(R.id.etSystolic)
        etDiastolic = findViewById(R.id.etDiastolic)
        etHeartRate = findViewById(R.id.etHeartRate)
        rgSmoker = findViewById(R.id.rgSmoker)
        rgAlcohol = findViewById(R.id.rgAlcohol)
        rgFamilyHistory = findViewById(R.id.rgFamilyHistory)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupGenderDropdown() {
        val genders = arrayOf("Male", "Female")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genders)
        etGender.setAdapter(adapter)
    }

    private fun initONNX() {
        try {
            ortEnv = OrtEnvironment.getEnvironment()
            // Pastikan nama file ini SAMA PERSIS dengan file di folder assets
            val modelFile = File(filesDir, "random_forest_model_scaled.onnx")
            copyAssetToInternalStorage("random_forest_model_scaled.onnx", modelFile)

            session = ortEnv.createSession(modelFile.absolutePath, OrtSession.SessionOptions())
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal memuat model: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    // --- LOGIKA MANUAL (SAFETY NET) ---
    // Ini logika untuk menentukan "Sakit" secara medis, jaga-jaga kalau AI salah.
    private fun checkHighRiskManual(
        age: Int, bmi: Float, systolic: Int, diastolic: Int,
        smoker: Int, alcohol: Int, familyHistory: Int
    ): Boolean {
        var riskScore = 0

        // 1. Hipertensi Berat (Jelas Sakit)
        if (systolic >= 140 || diastolic >= 90) riskScore += 2

        // 2. Obesitas Parah
        if (bmi >= 30.0f) riskScore += 1

        // 3. Merokok + Alkohol (Gaya Hidup Buruk)
        if (smoker == 1 && alcohol == 1) riskScore += 2

        // 4. Faktor Umur + Keturunan
        if (age > 50 && familyHistory == 1) riskScore += 1

        // Jika Skor >= 2, kita anggap dia BERISIKO
        return riskScore >= 2
    }

    // Fungsi Standard Scaler: (Value - Mean) / StdDev
    private fun scaleValue(value: Float, index: Int): Float {
        val mean = MEANS[index]
        val std = STDS[index]
        return if (std != 0f) (value - mean) / std else 0f
    }

    private fun performPrediction() {
        try {
            // 1. Ambil Input UI
            val age = etAge.text.toString().toInt()
            val genderStr = etGender.text.toString()
            val bmi = etBMI.text.toString().toFloat()
            val dailySteps = etDailySteps.text.toString().toInt()
            val sleepHours = etSleepHours.text.toString().toFloat()
            val waterIntake = etWaterIntake.text.toString().toFloat()
            val calories = etCalories.text.toString().toInt()
            val systolic = etSystolic.text.toString().toInt()
            val diastolic = etDiastolic.text.toString().toInt()
            val heartRate = etHeartRate.text.toString().toInt()

            // Input Biner (0 atau 1)
            val smoker = if (rgSmoker.checkedRadioButtonId == R.id.rbSmokerYes) 1 else 0
            val alcohol = if (rgAlcohol.checkedRadioButtonId == R.id.rbAlcoholYes) 1 else 0
            val familyHistory = if (rgFamilyHistory.checkedRadioButtonId == R.id.rbFamilyYes) 1 else 0
            val genderModel = if (genderStr.equals("Male", ignoreCase = true)) 1f else 0f

            // 2. Terapkan Scaling (HANYA PADA ANGKA, BUKAN PADA BINARY)
            // Urutan Array MEANS/STDS: age, bmi, steps, sleep, water, calories, hr, sys, dia
            val inputArray = floatArrayOf(
                scaleValue(age.toFloat(), 0),        // age
                genderModel,                         // gender (TIDAK DI-SCALE)
                scaleValue(bmi, 1),                  // bmi
                scaleValue(dailySteps.toFloat(), 2), // daily_steps
                scaleValue(sleepHours, 3),           // sleep_hours
                scaleValue(waterIntake, 4),          // water_intake
                scaleValue(calories.toFloat(), 5),   // calories
                smoker.toFloat(),                    // smoker (TIDAK DI-SCALE)
                alcohol.toFloat(),                   // alcohol (TIDAK DI-SCALE)
                scaleValue(heartRate.toFloat(), 6),  // resting_hr
                scaleValue(systolic.toFloat(), 7),   // systolic_bp
                scaleValue(diastolic.toFloat(), 8),  // diastolic_bp
                familyHistory.toFloat()              // family_history (TIDAK DI-SCALE)
            )

            // 3. Jalankan AI
            val shape = longArrayOf(1, 13)
            val buffer = FloatBuffer.wrap(inputArray)
            val inputTensor = OnnxTensor.createTensor(ortEnv, buffer, shape)
            val results = session.run(Collections.singletonMap("float_input", inputTensor))

            // 4. Ambil Hasil AI
            // Random Forest (sklearn) outputnya biasanya LongArray [label] dan FloatArray [probabilities]
            // Kita coba ambil labelnya dulu
            val outputLabel = results[0].value as LongArray // Label Kelas (0 atau 1)
            var predictionClass = outputLabel[0]

            // Coba ambil probabilitas (Biasanya ada di index 1 output Map zipmap=False)
            // Kalau error, kita pakai default dummy dulu
            var riskProbability = 0.0f
            try {
                // Logic untuk mengambil probabilitas dari ONNX sklearn bervariasi
                // Kita set sederhana: Jika kelas 1 -> 75%, Jika kelas 0 -> 25%
                // Kecuali kamu sudah setting output probabilitas di Python
                riskProbability = if (predictionClass == 1L) 0.85f else 0.15f
            } catch (e: Exception) {
                Log.e("PROB_ERROR", "Gagal ambil probabilitas detail")
            }

            // 5. HYBRID CHECK (Jaring Pengaman)
            // Apakah secara manual orang ini parah?
            val isHighRiskManual = checkHighRiskManual(
                age, bmi, systolic, diastolic, smoker, alcohol, familyHistory
            )

            // 6. KEPUTUSAN FINAL (Override AI jika perlu)
            if (predictionClass == 0L && isHighRiskManual) {
                // AI Bilang Sehat, TAPI Medis bilang Bahaya -> PAKSA SAKIT
                predictionClass = 1L
                riskProbability = 0.90f // Set probabilitas tinggi
                Toast.makeText(this, "Analisis Lanjutan Mendeteksi Risiko Tinggi", Toast.LENGTH_SHORT).show()
            }

            // 7. Pindah ke ResultActivity
            // Kita bungkus data untuk dikirim
            val userInput = UserInputModel(
                age = age, gender = genderStr, bmi = bmi,
                dailySteps = dailySteps, sleepHours = sleepHours,
                smoker = smoker, alcohol = alcohol,
                waterIntake = waterIntake, caloriesConsumed = calories,
                systolicBp = systolic, diastolicBp = diastolic,
                heartRate = heartRate, familyHistory = familyHistory
            )

            val intent = Intent(this, ResultActivity::class.java).apply {
                putExtra("USER_INPUT", userInput)
                putExtra("IS_AT_RISK", predictionClass == 1L)
                putExtra("RISK_PROBABILITY", riskProbability)
            }
            startActivity(intent)

            inputTensor.close()
            results.close()

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun validateInputs(): Boolean {
        if (etAge.text.isNullOrEmpty() || etBMI.text.isNullOrEmpty() ||
            etGender.text.isNullOrEmpty() || etDailySteps.text.isNullOrEmpty() ||
            etSleepHours.text.isNullOrEmpty() || etWaterIntake.text.isNullOrEmpty() ||
            etCalories.text.isNullOrEmpty() || etSystolic.text.isNullOrEmpty() ||
            etDiastolic.text.isNullOrEmpty() || etHeartRate.text.isNullOrEmpty()) {
            Toast.makeText(this, "Harap isi semua field!", Toast.LENGTH_SHORT).show()
            return false
        }
        if (rgSmoker.checkedRadioButtonId == -1 || rgAlcohol.checkedRadioButtonId == -1 || rgFamilyHistory.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Pilih semua opsi radio button!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun copyAssetToInternalStorage(fileName: String, outputFile: File) {
        if (!outputFile.exists()) {
            assets.open(fileName).use { inputStream ->
                FileOutputStream(outputFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::session.isInitialized) session.close()
        if (::ortEnv.isInitialized) ortEnv.close()
    }
}