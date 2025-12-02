package com.example.prediksipenyakit

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import android.widget.RadioGroup
import android.widget.ImageView

class MainActivity : AppCompatActivity() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupGenderDropdown()
        setupListeners()
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
        val genders = arrayOf("Laki-laki", "Perempuan")
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            genders
        )
        etGender.setAdapter(adapter)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        btnSubmit.setOnClickListener {
            if (validateInputs()) {
                submitData()
            }
        }
    }

    private fun validateInputs(): Boolean {
        // Validate BMI
        if (etBMI.text.isNullOrEmpty()) {
            etBMI.error = "BMI harus diisi"
            etBMI.requestFocus()
            return false
        }

        // Validate Age
        if (etAge.text.isNullOrEmpty()) {
            etAge.error = "Umur harus diisi"
            etAge.requestFocus()
            return false
        }

        // Validate Gender
        if (etGender.text.isNullOrEmpty()) {
            etGender.error = "Gender harus dipilih"
            etGender.requestFocus()
            return false
        }

        // Validate Daily Steps
        if (etDailySteps.text.isNullOrEmpty()) {
            etDailySteps.error = "Langkah harian harus diisi"
            etDailySteps.requestFocus()
            return false
        }

        // Validate Sleep Hours
        if (etSleepHours.text.isNullOrEmpty()) {
            etSleepHours.error = "Jam tidur harus diisi"
            etSleepHours.requestFocus()
            return false
        }

        // Validate Smoker
        if (rgSmoker.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Pilih status perokok", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validate Alcohol
        if (rgAlcohol.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Pilih status konsumsi alkohol", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validate Water Intake
        if (etWaterIntake.text.isNullOrEmpty()) {
            etWaterIntake.error = "Konsumsi air harus diisi"
            etWaterIntake.requestFocus()
            return false
        }

        // Validate Calories
        if (etCalories.text.isNullOrEmpty()) {
            etCalories.error = "Kalori harus diisi"
            etCalories.requestFocus()
            return false
        }

        // Validate Systolic
        if (etSystolic.text.isNullOrEmpty()) {
            etSystolic.error = "Tekanan sistolik harus diisi"
            etSystolic.requestFocus()
            return false
        }

        // Validate Diastolic
        if (etDiastolic.text.isNullOrEmpty()) {
            etDiastolic.error = "Tekanan diastolik harus diisi"
            etDiastolic.requestFocus()
            return false
        }

        // Validate Heart Rate
        if (etHeartRate.text.isNullOrEmpty()) {
            etHeartRate.error = "Detak jantung harus diisi"
            etHeartRate.requestFocus()
            return false
        }

        // Validate Cholesterol
        if (etCholesterol.text.isNullOrEmpty()) {
            etCholesterol.error = "Kolesterol harus diisi"
            etCholesterol.requestFocus()
            return false
        }

        return true
    }

    private fun submitData() {
        val data = HealthData(
            bmi = etBMI.text.toString().toFloat(),
            age = etAge.text.toString().toInt(),
            gender = etGender.text.toString(),
            dailySteps = etDailySteps.text.toString().toInt(),
            sleepHours = etSleepHours.text.toString().toFloat(),
            smoker = if (rgSmoker.checkedRadioButtonId == R.id.rbSmokerYes) 1 else 0,
            alcohol = if (rgAlcohol.checkedRadioButtonId == R.id.rbAlcoholYes) 1 else 0,
            waterIntake = etWaterIntake.text.toString().toFloat(),
            calories = etCalories.text.toString().toInt(),
            systolic = etSystolic.text.toString().toInt(),
            diastolic = etDiastolic.text.toString().toInt(),
            heartRate = etHeartRate.text.toString().toInt(),
            cholesterol = etCholesterol.text.toString().toInt()
        )

        // TODO: Send data to API or process it
        Toast.makeText(this, "Data berhasil dikirim!", Toast.LENGTH_SHORT).show()

        // Here you can add API call or navigate to result screen
        // Example: sendToAPI(data)
    }

    data class HealthData(
        val bmi: Float,
        val age: Int,
        val gender: String,
        val dailySteps: Int,
        val sleepHours: Float,
        val smoker: Int,
        val alcohol: Int,
        val waterIntake: Float,
        val calories: Int,
        val systolic: Int,
        val diastolic: Int,
        val heartRate: Int,
        val cholesterol: Int
    )
}