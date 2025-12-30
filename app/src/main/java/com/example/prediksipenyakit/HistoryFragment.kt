package com.example.prediksipenyakit

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var loadingBar: ProgressBar
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var tvEmptyState: TextView
    private lateinit var cardSummary: MaterialCardView
    private lateinit var tvTotalCheck: TextView

    // Chips
    private lateinit var chipAll: Chip
    private lateinit var chip7Days: Chip
    private lateinit var chipMonth: Chip
    private lateinit var chipPickDate: Chip // Tambahan

    private var allHistoryData: List<PredictionHistoryModel> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi View
        rvHistory = view.findViewById(R.id.rvHistory)
        loadingBar = view.findViewById(R.id.progressBar)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)
        cardSummary = view.findViewById(R.id.cardSummary)
        tvTotalCheck = view.findViewById(R.id.tvTotalCheck)

        chipAll = view.findViewById(R.id.chipAll)
        chip7Days = view.findViewById(R.id.chip7Days)
        chipMonth = view.findViewById(R.id.chipMonth)
        chipPickDate = view.findViewById(R.id.chipPickDate) // Init Chip Baru

        rvHistory.layoutManager = LinearLayoutManager(context)

        // Setup Adapter
        adapter = HistoryAdapter(emptyList()) { historyItem ->
            bukaDetailHistory(historyItem)
        }
        rvHistory.adapter = adapter

        // Setup Filter Chips
        setupFilterChips()

        // Ambil Data
        fetchHistory()
    }

    private fun setupFilterChips() {
        chipAll.setOnClickListener { filterData(FilterType.ALL) }
        chip7Days.setOnClickListener { filterData(FilterType.SEVEN_DAYS) }
        chipMonth.setOnClickListener { filterData(FilterType.THIS_MONTH) }

        // Listener untuk Pick Date
        chipPickDate.setOnClickListener {
            showDatePicker()
        }
    }

    // Fungsi Menampilkan Kalender
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Saat user memilih tanggal
                val selectedCal = Calendar.getInstance()
                selectedCal.set(selectedYear, selectedMonth, selectedDay)

                // Ubah text chip jadi tanggal yg dipilih
                val formatTitle = SimpleDateFormat("dd MMM", Locale("id", "ID"))
                chipPickDate.text = formatTitle.format(selectedCal.time)

                // Jalankan Filter Spesifik
                filterData(FilterType.SPECIFIC_DATE, selectedCal.time)
            },
            year, month, day
        )

        // Aksi jika user batal pilih (Cancel)
        datePickerDialog.setOnCancelListener {
            // Kembalikan ke "Semua" atau biarkan state sebelumnya
            chipAll.isChecked = true
            filterData(FilterType.ALL)
            chipPickDate.text = "Pilih Tanggal"
        }

        datePickerDialog.show()
    }

    private fun filterData(filterType: FilterType, specificDate: Date? = null) {
        // Reset text chip tanggal jika pindah filter lain
        if (filterType != FilterType.SPECIFIC_DATE) {
            chipPickDate.text = "Pilih Tanggal"
        }

        val filteredList = when (filterType) {
            FilterType.ALL -> allHistoryData
            FilterType.SEVEN_DAYS -> {
                val sevenDaysAgo = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -7)
                }.time
                allHistoryData.filter { item ->
                    try {
                        val itemDate = parseDate(item.createdAt)
                        itemDate?.after(sevenDaysAgo) ?: false
                    } catch (e: Exception) { false }
                }
            }
            FilterType.THIS_MONTH -> {
                val thisMonth = Calendar.getInstance().get(Calendar.MONTH)
                val thisYear = Calendar.getInstance().get(Calendar.YEAR)
                allHistoryData.filter { item ->
                    try {
                        val itemDate = parseDate(item.createdAt)
                        val itemCal = Calendar.getInstance().apply { time = itemDate ?: Date() }
                        itemCal.get(Calendar.MONTH) == thisMonth &&
                                itemCal.get(Calendar.YEAR) == thisYear
                    } catch (e: Exception) { false }
                }
            }
            FilterType.SPECIFIC_DATE -> {
                if (specificDate == null) allHistoryData
                else {
                    val targetCal = Calendar.getInstance().apply { time = specificDate }
                    allHistoryData.filter { item ->
                        try {
                            val itemDate = parseDate(item.createdAt) ?: return@filter false
                            val itemCal = Calendar.getInstance().apply { time = itemDate }

                            // Bandingkan Hari, Bulan, dan Tahun
                            itemCal.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR) &&
                                    itemCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR)
                        } catch (e: Exception) { false }
                    }
                }
            }
        }

        adapter.updateData(filteredList)
        updateSummaryCard(filteredList.size)

        // Update UI Empty State jika hasil filter kosong
        if (filteredList.isEmpty()) {
            emptyStateLayout.visibility = View.VISIBLE
            tvEmptyState.text = "Tidak ada data pada tanggal ini"
            rvHistory.visibility = View.GONE
        } else {
            emptyStateLayout.visibility = View.GONE
            rvHistory.visibility = View.VISIBLE
        }
    }

    // Helper parse date biar rapi
    private fun parseDate(dateString: String?): Date? {
        if (dateString == null) return null
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(dateString)
    }

    private fun bukaDetailHistory(item: PredictionHistoryModel) {
        val dataInput = UserInputModel(
            age = item.age,
            gender = item.gender,
            bmi = item.bmi,
            dailySteps = item.dailySteps,
            sleepHours = item.sleepHours,
            smoker = item.smoker,
            alcohol = item.alcohol,
            waterIntake = item.waterIntake,
            caloriesConsumed = item.calories,
            systolicBp = item.systolicBp,
            diastolicBp = item.diastolicBp,
            heartRate = item.heartRate,
            familyHistory = item.familyHistory
        )

        val intent = Intent(requireContext(), ResultActivity::class.java).apply {
            putExtra("USER_INPUT", dataInput)
            putExtra("IS_AT_RISK", item.predictionResult == 1)
            putExtra("RISK_PROBABILITY", item.riskScore)
        }
        startActivity(intent)
    }

    private fun fetchHistory() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                val userId = currentUser?.id

                if (userId != null) {
                    val result = SupabaseClient.client.from("prediction_history")
                        .select {
                            filter { eq("user_id", userId) }
                            order("created_at", order = Order.DESCENDING)
                        }.decodeList<PredictionHistoryModel>()

                    allHistoryData = result

                    withContext(Dispatchers.Main) {
                        loadingBar.visibility = View.GONE
                        if (result.isNotEmpty()) {
                            // Default tampilkan semua saat awal load
                            filterData(FilterType.ALL)
                            cardSummary.visibility = View.VISIBLE
                            emptyStateLayout.visibility = View.GONE
                            rvHistory.visibility = View.VISIBLE
                        } else {
                            emptyStateLayout.visibility = View.VISIBLE
                            tvEmptyState.text = "Belum ada riwayat prediksi"
                            cardSummary.visibility = View.GONE
                            rvHistory.visibility = View.GONE
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HISTORY", "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    loadingBar.visibility = View.GONE
                    tvEmptyState.text = "Gagal memuat data."
                    emptyStateLayout.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun updateSummaryCard(count: Int) {
        tvTotalCheck.text = "$count kali"
    }

    override fun onResume() {
        super.onResume()
        fetchHistory()
    }

    enum class FilterType {
        ALL, SEVEN_DAYS, THIS_MONTH, SPECIFIC_DATE
    }
}