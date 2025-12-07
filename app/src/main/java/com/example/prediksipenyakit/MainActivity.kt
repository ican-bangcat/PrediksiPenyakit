package com.example.prediksipenyakit

import android.content.Intent
import android.os.Bundle
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

class MainActivity : AppCompatActivity() {

    // UI Components
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

    // ONNX Components
    private lateinit var ortEnv: OrtEnvironment
    private lateinit var session: OrtSession

    // Scaling Parameters (dari StandardScaler Python) - CORRECTED
    companion object {
        // Mean values untuk setiap fitur (urutan sesuai training)
        // Berdasarkan screenshot yang benar (tanpa kolom id)
        private val MEANS = floatArrayOf(
            48.525990f,    // age
            29.024790f,    // bmi
            10479.87029f,  // daily_steps
            6.491784f,     // sleep_hours
            2.751496f,     // water_intake_l
            2603.341200f,  // calories_consumed
            0.200940f,     // smoker
            0.300020f,     // alcohol
            74.457420f,    // resting_hr
            134.58063f,    // systolic_bp
            89.508850f,    // diastolic_bp
            0.298150f,     // family_history
            0.248210f      // disease_risk (output, tidak dipakai)
        )

        // Standard Deviation untuk setiap fitur
        private val STDS = floatArrayOf(
            17.886768f,    // age std
            6.352666f,     // bmi std
            5483.63236f,   // daily_steps std
            2.021922f,     // sleep_hours std
            1.297338f,     // water_intake_l std
            807.288563f,   // calories_consumed std
            0.400705f,     // smoker std
            0.458269f,     // alcohol std
            14.423715f,    // resting_hr std
            25.95153f,     // systolic_bp std
            17.347041f,    // diastolic_bp std
            0.457888f,     // family_history std
            0.431976f      // disease_risk std (output, tidak dipakai)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
            // GANTI: Model baru Random Forest dengan scaling
            val modelFile = File(filesDir, "random_forest_model_scaled.onnx")
            copyAssetToInternalStorage("random_forest_model_scaled.onnx", modelFile)

            session = ortEnv.createSession(modelFile.absolutePath, OrtSession.SessionOptions())
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal memuat model: ${e.message}", Toast.LENGTH_LONG).show()
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

        if (rgSmoker.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Pilih status merokok!", Toast.LENGTH_SHORT).show()
            return false
        }

        if (rgAlcohol.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Pilih status alkohol!", Toast.LENGTH_SHORT).show()
            return false
        }

        if (rgFamilyHistory.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Pilih riwayat penyakit keluarga!", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    /**
     * Fungsi untuk melakukan Standard Scaling
     * Formula: (value - mean) / std
     */
    private fun scaleValue(value: Float, mean: Float, std: Float): Float {
        return if (std != 0f) {
            (value - mean) / std
        } else {
            0f // Hindari division by zero
        }
    }

    private fun performPrediction() {
        try {
            // --- 1. Ambil Data Raw dari Input UI ---
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

            // Radio Button (1 = Ya, 0 = Tidak) - TIDAK DI-SCALE
            val smoker = if (rgSmoker.checkedRadioButtonId == R.id.rbSmokerYes) 1f else 0f
            val alcohol = if (rgAlcohol.checkedRadioButtonId == R.id.rbAlcoholYes) 1f else 0f
            val familyHistory = if (rgFamilyHistory.checkedRadioButtonId == R.id.rbFamilyYes) 1f else 0f

            // Gender Mapping - TIDAK DI-SCALE (sudah binary)
            val genderModel = if (genderStr.equals("Male", ignoreCase = true)) 1f else 0f

            // --- 2. Apply Scaling pada Fitur Numerik ---
            // Urutan sesuai dengan training Python:
            // age, bmi, daily_steps, sleep_hours, water_intake_l,
            // calories_consumed, smoker, alcohol, resting_hr, systolic_bp,
            // diastolic_bp, family_history

            // Index mapping untuk MEANS dan STDS:
            // 0: age, 1: bmi, 2: daily_steps, 3: sleep_hours, 4: water_intake
            // 5: calories, 6: smoker, 7: alcohol, 8: resting_hr
            // 9: systolic_bp, 10: diastolic_bp, 11: family_history

            val ageScaled = scaleValue(age.toFloat(), MEANS[0], STDS[0])
            val bmiScaled = scaleValue(bmi, MEANS[1], STDS[1])
            val dailyStepsScaled = scaleValue(dailySteps.toFloat(), MEANS[2], STDS[2])
            val sleepHoursScaled = scaleValue(sleepHours, MEANS[3], STDS[3])
            val waterIntakeScaled = scaleValue(waterIntake, MEANS[4], STDS[4])
            val caloriesScaled = scaleValue(calories.toFloat(), MEANS[5], STDS[5])
            val smokerScaled = scaleValue(smoker, MEANS[6], STDS[6])
            val alcoholScaled = scaleValue(alcohol, MEANS[7], STDS[7])
            val heartRateScaled = scaleValue(heartRate.toFloat(), MEANS[8], STDS[8])
            val systolicScaled = scaleValue(systolic.toFloat(), MEANS[9], STDS[9])
            val diastolicScaled = scaleValue(diastolic.toFloat(), MEANS[10], STDS[10])
            val familyHistoryScaled = scaleValue(familyHistory, MEANS[11], STDS[11])

            // --- 3. Buat Array Input SCALED untuk Model ---
            val inputArray = floatArrayOf(
                ageScaled,           // 0: age (SCALED)
                genderModel,         // 1: gender (binary, tidak di-scale)
                bmiScaled,           // 2: bmi (SCALED)
                dailyStepsScaled,    // 3: daily_steps (SCALED)
                sleepHoursScaled,    // 4: sleep_hours (SCALED)
                waterIntakeScaled,   // 5: water_intake_l (SCALED)
                caloriesScaled,      // 6: calories_consumed (SCALED)
                smokerScaled,        // 7: smoker (SCALED)
                alcoholScaled,       // 8: alcohol (SCALED)
                heartRateScaled,     // 9: resting_hr (SCALED)
                systolicScaled,      // 10: systolic_bp (SCALED)
                diastolicScaled,     // 11: diastolic_bp (SCALED)
                familyHistoryScaled  // 12: family_history (SCALED)
            )

            // Debug: Log nilai scaled
            android.util.Log.d("Prediction", "Scaled Input: ${inputArray.contentToString()}")

            // --- 4. Buat Tensor ONNX ---
            val shape = longArrayOf(1, 13)
            val buffer = FloatBuffer.wrap(inputArray)
            val inputTensor = OnnxTensor.createTensor(ortEnv, buffer, shape)

            // --- 5. Jalankan Prediksi ---
            val results = session.run(Collections.singletonMap("float_input", inputTensor))

            // --- 6. Ambil Output ---
            val outputTensor = results[0]
            val resultValue = outputTensor.value as LongArray
            val predictionClass = resultValue[0]

            // Hitung probabilitas (bisa juga ambil dari output[1] jika model support)
            val riskProbability = if (predictionClass == 1L) 0.75f else 0.25f

            // --- 7. Buat UserInputModel untuk kirim ke ResultActivity ---
            val userInput = UserInputModel(
                age = age,
                gender = genderStr,
                bmi = bmi,
                dailySteps = dailySteps,
                sleepHours = sleepHours,
                smoker = smoker.toInt(),
                alcohol = alcohol.toInt(),
                waterIntake = waterIntake,
                caloriesConsumed = calories,
                systolicBp = systolic,
                diastolicBp = diastolic,
                heartRate = heartRate,
                familyHistory = familyHistory.toInt()
            )

            // --- 8. Pindah ke ResultActivity ---
            val intent = Intent(this, ResultActivity::class.java).apply {
                putExtra("USER_INPUT", userInput)
                putExtra("IS_AT_RISK", predictionClass == 1L)
                putExtra("RISK_PROBABILITY", riskProbability)
            }
            startActivity(intent)

            // Bersihkan memori
            inputTensor.close()
            results.close()

        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Format input tidak valid! Periksa kembali angka yang dimasukkan.", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        } catch (e: Exception) {
            Toast.makeText(this, "Error Prediksi: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
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