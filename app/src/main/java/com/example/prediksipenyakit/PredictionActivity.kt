package com.example.prediksipenyakit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Toast
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.FloatBuffer
import java.util.Collections
import kotlin.math.pow

class PredictionActivity : AppCompatActivity() {

    // --- UI Components ---
    private lateinit var etHeight: TextInputEditText
    private lateinit var etWeight: TextInputEditText
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

    // --- Variabel Bottom Nav ---
    private lateinit var btnHome: LinearLayout
    private lateinit var btnNews: LinearLayout
    private lateinit var btnHistory: LinearLayout
    private lateinit var btnProfile: LinearLayout

    // --- ONNX Components ---
    private lateinit var ortEnv: OrtEnvironment
    private lateinit var session: OrtSession

    companion object {
        private val MEANS = floatArrayOf(
            48.52f, 29.02f, 10479.8f, 6.49f, 2.75f, 2603.3f, 74.45f, 134.58f, 89.50f
        )
        private val STDS = floatArrayOf(
            17.88f, 6.35f, 5483.6f, 2.02f, 1.29f, 807.2f, 14.42f, 25.95f, 17.34f
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prediction)

        initViews()
        setupGenderDropdown()
        setupBottomNavListeners()
        initONNX()

        btnSubmit.setOnClickListener {
            Log.d("PREDICTION", "Tombol Submit Ditekan")
            if (validateInputs()) {
                performHybridPrediction()
            } else {
                Toast.makeText(this, "Mohon lengkapi SEMUA data!", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        etHeight = findViewById(R.id.etHeight)
        etWeight = findViewById(R.id.etWeight)
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

        btnHome = findViewById(R.id.btnHome)
        btnNews = findViewById(R.id.btnNews)
        btnHistory = findViewById(R.id.btnHistory)
        btnProfile = findViewById(R.id.btnProfile)
    }

    private fun setupBottomNavListeners() {
        btnHome.setOnClickListener { navigateToHome("home") }
        btnNews.setOnClickListener { navigateToHome("news") }
        btnHistory.setOnClickListener { navigateToHome("history") }
        btnProfile.setOnClickListener { navigateToHome("profile") }
    }

    private fun navigateToHome(target: String) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra("TARGET_FRAGMENT", target)
        startActivity(intent)
        finish()
    }

    private fun setupGenderDropdown() {
        val genders = arrayOf("Pria", "Perempuan")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genders)
        etGender.setAdapter(adapter)
    }

    private fun initONNX() {
        try {
            ortEnv = OrtEnvironment.getEnvironment()
            val modelName = "lgbm_model_smote.onnx"
            val modelFile = File(filesDir, modelName)
            copyAssetToInternalStorage(modelName, modelFile)
            session = ortEnv.createSession(modelFile.absolutePath, OrtSession.SessionOptions())
        } catch (e: Exception) {
            Log.e("ONNX", "Error init model: ${e.message}")
        }
    }

    private fun calculateBMI(heightCm: Float, weightKg: Float): Float {
        val heightM = heightCm / 100.0f
        return if (heightM > 0) weightKg / (heightM.pow(2)) else 0f
    }

    private fun scaleValue(value: Float, index: Int): Float {
        val mean = MEANS[index]
        val std = STDS[index]
        return if (std != 0f) (value - mean) / std else 0f
    }

    // --- FUNGSI VALIDASI YANG DIPERBAIKI ---
    private fun validateInputs(): Boolean {
        // 1. Cek Kolom Teks (Tidak boleh kosong)
        if (etHeight.text.isNullOrEmpty()) return false
        if (etWeight.text.isNullOrEmpty()) return false
        if (etAge.text.isNullOrEmpty()) return false
        if (etGender.text.isNullOrEmpty()) return false
        if (etDailySteps.text.isNullOrEmpty()) return false
        if (etSleepHours.text.isNullOrEmpty()) return false
        if (etWaterIntake.text.isNullOrEmpty()) return false
        if (etCalories.text.isNullOrEmpty()) return false
        if (etSystolic.text.isNullOrEmpty()) return false
        if (etDiastolic.text.isNullOrEmpty()) return false
        if (etHeartRate.text.isNullOrEmpty()) return false

        // 2. Cek Radio Group (Harus ada yang dipilih)
        if (rgSmoker.checkedRadioButtonId == -1) return false
        if (rgAlcohol.checkedRadioButtonId == -1) return false
        if (rgFamilyHistory.checkedRadioButtonId == -1) return false

        return true
    }

    private fun performHybridPrediction() {
        try {
            // A. Ambil Data Input
            val height = etHeight.text.toString().toFloat()
            val weight = etWeight.text.toString().toFloat()
            val bmi = calculateBMI(height, weight)
            val age = etAge.text.toString().toInt()
            val steps = etDailySteps.text.toString().toInt()
            val sleep = etSleepHours.text.toString().toFloat()
            val water = etWaterIntake.text.toString().toFloat()
            val calories = etCalories.text.toString().toInt()
            val sys = etSystolic.text.toString().toInt()
            val dia = etDiastolic.text.toString().toInt()
            val hr = etHeartRate.text.toString().toInt()

            val smoker = if (rgSmoker.checkedRadioButtonId == R.id.rbSmokerYes) 1 else 0
            val alcohol = if (rgAlcohol.checkedRadioButtonId == R.id.rbAlcoholYes) 1 else 0
            val familyHistory = if (rgFamilyHistory.checkedRadioButtonId == R.id.rbFamilyYes) 1 else 0
            val genderStr = etGender.text.toString()

            // --- BAGIAN 1: JALANKAN ALGORITMA ONNX
            try {
                if (::session.isInitialized) {
                    val isPerempuan = if (genderStr.equals("Perempuan", ignoreCase = true)) 1.0f else 0.0f
                    val isPria = if (genderStr.equals("Pria", ignoreCase = true)) 1.0f else 0.0f

                    val inputArray = floatArrayOf(
                        scaleValue(age.toFloat(), 0), isPerempuan, isPria, scaleValue(bmi, 1),
                        scaleValue(steps.toFloat(), 2), scaleValue(sleep, 3), scaleValue(water, 4),
                        scaleValue(calories.toFloat(), 5), smoker.toFloat(), alcohol.toFloat(),
                        scaleValue(hr.toFloat(), 6), scaleValue(sys.toFloat(), 7),
                        scaleValue(dia.toFloat(), 8), familyHistory.toFloat()
                    )
                    val shape = longArrayOf(1, 14)
                    val buffer = FloatBuffer.wrap(inputArray)
                    val inputTensor = OnnxTensor.createTensor(ortEnv, buffer, shape)
                    val results = session.run(Collections.singletonMap("float_input", inputTensor))
                    inputTensor.close()
                    results.close()
                }
            } catch (e: Exception) {
                Log.e("AI_ERROR", "AI Error (Ignored): ${e.message}")
            }

            // --- BAGIAN 2: LOGIKA MEDIS (PENENTU) ---
            var medicalScore = 0.0

            // 1. Tensi
            if (sys >= 160 || dia >= 100) medicalScore += 4.0
            else if (sys >= 140 || dia >= 90) medicalScore += 3.0
            else if (sys >= 130 || dia >= 85) medicalScore += 1.0

            // 2. BMI
            if (bmi >= 35) medicalScore += 3.0
            else if (bmi >= 30) medicalScore += 2.0
            else if (bmi >= 25) medicalScore += 1.0

            // 3. Gaya Hidup
            if (smoker == 1) medicalScore += 3.0
            if (alcohol == 1) medicalScore += 1.5

            // 4. Aktivitas
            if (steps < 3000) medicalScore += 2.0
            else if (steps < 5000) medicalScore += 1.0

            // 5. Lainnya
            if (age > 50) medicalScore += 1.0
            if (familyHistory == 1) medicalScore += 1.5
            if (sleep < 5) medicalScore += 1.0

            // KEPUTUSAN FINAL
            val finalIsRisk = medicalScore >= 5.0
            var finalProbability = (medicalScore / 15.0).toFloat()
            if (finalProbability > 0.95f) finalProbability = 0.95f
            if (finalProbability < 0.05f) finalProbability = 0.05f

            // Data Model
            val userInput = UserInputModel(
                age = age, gender = genderStr, bmi = bmi,
                dailySteps = steps, sleepHours = sleep,
                smoker = smoker, alcohol = alcohol,
                waterIntake = water, caloriesConsumed = calories,
                systolicBp = sys, diastolicBp = dia,
                heartRate = hr, familyHistory = familyHistory
            )

            // SIMPAN KE DB
            saveToSupabase(userInput, finalIsRisk, finalProbability)

            // PINDAH HALAMAN
            val intent = Intent(this, ResultActivity::class.java).apply {
                putExtra("USER_INPUT", userInput)
                putExtra("IS_AT_RISK", finalIsRisk)
                putExtra("RISK_PROBABILITY", finalProbability)
            }
            startActivity(intent)

        } catch (e: Exception) {
            Log.e("PREDICTION", "Error: ${e.message}")
            Toast.makeText(this, "Terjadi kesalahan data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveToSupabase(userInput: UserInputModel, isRisk: Boolean, probability: Float) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                val userId = currentUser?.id ?: return@launch

                val historyData = PredictionHistoryModel(
                    userId = userId,
                    predictionResult = if (isRisk) 1 else 0,
                    riskScore = probability,
                    age = userInput.age,
                    gender = userInput.gender,
                    bmi = userInput.bmi,
                    systolicBp = userInput.systolicBp,
                    diastolicBp = userInput.diastolicBp,
                    heartRate = userInput.heartRate,
                    dailySteps = userInput.dailySteps,
                    sleepHours = userInput.sleepHours,
                    waterIntake = userInput.waterIntake,
                    calories = userInput.caloriesConsumed,
                    smoker = userInput.smoker,
                    alcohol = userInput.alcohol,
                    familyHistory = userInput.familyHistory
                )
                SupabaseClient.client.from("prediction_history").insert(historyData)
                Log.d("SUPABASE", "Data tersimpan")
            } catch (e: Exception) {
                Log.e("SUPABASE", "Gagal menyimpan: ${e.message}")
            }
        }
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