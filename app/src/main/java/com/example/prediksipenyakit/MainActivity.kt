package com.example.prediksipenyakit

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
    private lateinit var etCholesterol: TextInputEditText
    private lateinit var rgSmoker: RadioGroup
    private lateinit var rgAlcohol: RadioGroup
    private lateinit var btnSubmit: MaterialButton
    private lateinit var btnBack: ImageView

    // ONNX Components
    private lateinit var ortEnv: OrtEnvironment
    private lateinit var session: OrtSession

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupGenderDropdown()

        // Inisialisasi ONNX Runtime
        initONNX()

        btnSubmit.setOnClickListener {
            performPrediction()
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
        etCholesterol = findViewById(R.id.etCholesterol)
        rgSmoker = findViewById(R.id.rgSmoker)
        rgAlcohol = findViewById(R.id.rgAlcohol)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupGenderDropdown() {
        // Pastikan opsi ini sesuai dengan LabelEncoder di Python
        val genders = arrayOf("Male", "Female")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, genders)
        etGender.setAdapter(adapter)
    }

    private fun initONNX() {
        try {
            ortEnv = OrtEnvironment.getEnvironment()
            // Pastikan nama file .onnx di folder assets SAMA PERSIS dengan string ini
            val modelFile = File(filesDir, "lgbm_model_smote.onnx")
            copyAssetToInternalStorage("lgbm_model_smote.onnx", modelFile)

            session = ortEnv.createSession(modelFile.absolutePath, OrtSession.SessionOptions())
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal memuat model: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun performPrediction() {
        try {
            // --- 1. Ambil Data dari Input UI ---
            val age = etAge.text.toString().toFloatOrNull() ?: 0f

            // Gender Mapping (Sesuaikan dengan Python: Male=1/0?)
            val genderStr = etGender.text.toString()
            val gender = if (genderStr.equals("Male", ignoreCase = true)) 1f else 0f

            val bmi = etBMI.text.toString().toFloatOrNull() ?: 0f
            val dailySteps = etDailySteps.text.toString().toFloatOrNull() ?: 0f
            val sleepHours = etSleepHours.text.toString().toFloatOrNull() ?: 0f
            val waterIntake = etWaterIntake.text.toString().toFloatOrNull() ?: 0f
            val calories = etCalories.text.toString().toFloatOrNull() ?: 0f

            // Radio Button (1 = Ya, 0 = Tidak)
            val smoker = if (rgSmoker.checkedRadioButtonId == R.id.rbSmokerYes) 1f else 0f
            val alcohol = if (rgAlcohol.checkedRadioButtonId == R.id.rbAlcoholYes) 1f else 0f

            val heartRate = etHeartRate.text.toString().toFloatOrNull() ?: 0f
            val systolic = etSystolic.text.toString().toFloatOrNull() ?: 0f
            val diastolic = etDiastolic.text.toString().toFloatOrNull() ?: 0f
            val cholesterol = etCholesterol.text.toString().toFloatOrNull() ?: 0f

            // [PENTING] Fitur Family History tidak ada di UI XML Anda.
            // Saya set default 0.0 (Tidak ada riwayat).
            // TODO: Tambahkan Checkbox di XML untuk input riwayat keluarga yang benar.
            val familyHistory = 0f


            // --- 2. Buat Array Input (Urutan HARUS SAMA PERSIS dengan Python) ---
            // Urutan Python: ['age', 'gender', 'bmi', 'daily_steps', 'sleep_hours', 'water_intake_l',
            // 'calories_consumed', 'smoker', 'alcohol', 'resting_hr', 'systolic_bp',
            // 'diastolic_bp', 'cholesterol', 'family_history']

            val inputArray = floatArrayOf(
                age,            // 1
                gender,         // 2
                bmi,            // 3
                dailySteps,     // 4
                sleepHours,     // 5
                waterIntake,    // 6
                calories,       // 7
                smoker,         // 8
                alcohol,        // 9
                heartRate,      // 10 (resting_hr)
                systolic,       // 11
                diastolic,      // 12
                cholesterol,    // 13
                familyHistory   // 14
            )

            // --- 3. Buat Tensor ONNX ---
            val shape = longArrayOf(1, 14) // Ukuran [1 baris, 14 kolom]
            val buffer = FloatBuffer.wrap(inputArray)
            val inputTensor = OnnxTensor.createTensor(ortEnv, buffer, shape)

            // --- 4. Jalankan Prediksi ---
            // "float_input" adalah nama input tensor saat convert di Python.
            val results = session.run(Collections.singletonMap("float_input", inputTensor))

            // --- 5. Ambil Output ---
            // Output index 0 biasanya adalah LABEL (Hasil prediksi kelas)
            val outputTensor = results[0]
            val resultValue = outputTensor.value as LongArray // LightGBM biasanya output Long (Int64)
            val predictionClass = resultValue[0]

            // Tampilkan Hasil
            val pesan = if (predictionClass == 1L) "Prediksi: BERISIKO (1)" else "Prediksi: AMAN (0)"
            Toast.makeText(this, pesan, Toast.LENGTH_LONG).show()

            // Bersihkan memori
            inputTensor.close()
            results.close()

        } catch (e: Exception) {
            Toast.makeText(this, "Error Prediksi: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace() // Cek Logcat untuk detail merahnya
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