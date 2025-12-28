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

    // --- ONNX Components ---
    private lateinit var ortEnv: OrtEnvironment
    private lateinit var session: OrtSession

    companion object {
        // SCALER: Hanya untuk 9 Fitur Numerik
        // Urutan Index di Array ini:
        // 0: age, 1: bmi, 2: steps, 3: sleep, 4: water, 5: cal, 6: hr, 7: sys, 8: dia
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
    }

    private fun setupGenderDropdown() {
        val genders = arrayOf("Male", "Female")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genders)
        etGender.setAdapter(adapter)
    }

    private fun initONNX() {
        try {
            ortEnv = OrtEnvironment.getEnvironment()
            // Pastikan nama file ini benar di folder assets
            val modelName = "lgbm_model_smote.onnx"
            val modelFile = File(filesDir, modelName)
            copyAssetToInternalStorage(modelName, modelFile)
            session = ortEnv.createSession(modelFile.absolutePath, OrtSession.SessionOptions())
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal memuat model: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun calculateBMI(heightCm: Float, weightKg: Float): Float {
        val heightM = heightCm / 100.0f
        return weightKg / (heightM.pow(2))
    }

    private fun scaleValue(value: Float, index: Int): Float {
        val mean = MEANS[index]
        val std = STDS[index]
        return if (std != 0f) (value - mean) / std else 0f
    }

    private fun checkHighRiskManual(
        age: Int, bmi: Float, systolic: Int, diastolic: Int,
        smoker: Int, alcohol: Int, familyHistory: Int
    ): Boolean {
        var riskScore = 0
        if (systolic >= 140 || diastolic >= 90) riskScore += 2
        if (bmi >= 30.0f) riskScore += 1
        if (smoker == 1 && alcohol == 1) riskScore += 2
        if (age > 50 && familyHistory == 1) riskScore += 1
        return riskScore >= 2
    }

    private fun performPrediction() {
        try {
            // 1. Ambil Input UI
            val heightCm = etHeight.text.toString().toFloat()
            val weightKg = etWeight.text.toString().toFloat()

            // Hitung BMI Otomatis
            val bmi = calculateBMI(heightCm, weightKg)

            val age = etAge.text.toString().toInt()
            val genderStr = etGender.text.toString()
            val dailySteps = etDailySteps.text.toString().toInt()
            val sleepHours = etSleepHours.text.toString().toFloat()
            val waterIntake = etWaterIntake.text.toString().toFloat()
            val calories = etCalories.text.toString().toInt()
            val systolic = etSystolic.text.toString().toInt()
            val diastolic = etDiastolic.text.toString().toInt()
            val heartRate = etHeartRate.text.toString().toInt()

            val smoker = if (rgSmoker.checkedRadioButtonId == R.id.rbSmokerYes) 1 else 0
            val alcohol = if (rgAlcohol.checkedRadioButtonId == R.id.rbAlcoholYes) 1 else 0
            val familyHistory = if (rgFamilyHistory.checkedRadioButtonId == R.id.rbFamilyYes) 1 else 0

            // --- LOGIKA GENDER ONE-HOT ENCODING (Memecah Gender jadi 2) ---
            val isFemale = if (genderStr.equals("Female", ignoreCase = true)) 1.0f else 0.0f
            val isMale = if (genderStr.equals("Male", ignoreCase = true)) 1.0f else 0.0f

            Log.d("PREDIKSI", "Input -> BMI: $bmi, Female: $isFemale, Male: $isMale")

            // 2. Susun Array Input (Total 14 Fitur)
            // Scaling tetap pakai index lama karena gender tidak di-scale
            val inputArray = floatArrayOf(
                scaleValue(age.toFloat(), 0),        // Age

                isFemale,                            // Gender_Female (Baru)
                isMale,                              // Gender_Male (Baru)

                scaleValue(bmi, 1),                  // BMI
                scaleValue(dailySteps.toFloat(), 2), // Steps
                scaleValue(sleepHours, 3),           // Sleep
                scaleValue(waterIntake, 4),          // Water
                scaleValue(calories.toFloat(), 5),   // Calories
                smoker.toFloat(),                    // Smoker (No Scale)
                alcohol.toFloat(),                   // Alcohol (No Scale)
                scaleValue(heartRate.toFloat(), 6),  // HR
                scaleValue(systolic.toFloat(), 7),   // Sys
                scaleValue(diastolic.toFloat(), 8),  // Dia
                familyHistory.toFloat()              // Family (No Scale)
            )

            // 3. Jalankan AI (Shape 1 baris, 14 kolom)
            val shape = longArrayOf(1, 14)
            val buffer = FloatBuffer.wrap(inputArray)
            val inputTensor = OnnxTensor.createTensor(ortEnv, buffer, shape)
            val results = session.run(Collections.singletonMap("float_input", inputTensor))

            // 4. Ambil Hasil
            val outputLabel = results[0].value as LongArray
            var predictionClass = outputLabel[0]

            // Dummy Probabilitas
            var riskProbability = if (predictionClass == 1L) 0.85f else 0.15f

            // 5. Hybrid Check
            val isHighRiskManual = checkHighRiskManual(
                age, bmi, systolic, diastolic, smoker, alcohol, familyHistory
            )
            if (predictionClass == 0L && isHighRiskManual) {
                predictionClass = 1L
                riskProbability = 0.90f
                Toast.makeText(this, "Analisis Medis Mendeteksi Risiko Tinggi", Toast.LENGTH_SHORT).show()
            }

            // 6. Kirim ke Result
            val userInput = UserInputModel(
                age = age,
                gender = genderStr,
                bmi = bmi,
                dailySteps = dailySteps,
                sleepHours = sleepHours,
                smoker = smoker,
                alcohol = alcohol,
                waterIntake = waterIntake,
                caloriesConsumed = calories,
                systolicBp = systolic,
                diastolicBp = diastolic,
                heartRate = heartRate,
                familyHistory = familyHistory
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
        if (etHeight.text.isNullOrEmpty() || etWeight.text.isNullOrEmpty()) {
            Toast.makeText(this, "Isi Tinggi dan Berat Badan!", Toast.LENGTH_SHORT).show()
            return false
        }
        // Validasi input lainnya...
        if (etAge.text.isNullOrEmpty() || etGender.text.isNullOrEmpty() ||
            etDailySteps.text.isNullOrEmpty()) {
            Toast.makeText(this, "Harap isi semua field!", Toast.LENGTH_SHORT).show()
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