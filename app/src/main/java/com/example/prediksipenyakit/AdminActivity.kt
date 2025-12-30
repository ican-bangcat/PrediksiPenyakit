package com.example.prediksipenyakit

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

class AdminActivity : AppCompatActivity() {

    // Variabel Navigasi
    private lateinit var btnHome: LinearLayout
    private lateinit var btnNews: LinearLayout
    private lateinit var btnHistory: LinearLayout
    private lateinit var btnProfile: LinearLayout
    private lateinit var btnFabPredict: CardView
    // private lateinit var btnLogout: Button <-- HAPUS INI

    // Variabel Icon & Text
    private lateinit var imgHome: ImageView
    private lateinit var textHome: TextView
    private lateinit var imgNews: ImageView
    private lateinit var textNews: TextView
    private lateinit var imgHistory: ImageView
    private lateinit var textHistory: TextView
    private lateinit var imgProfile: ImageView
    private lateinit var textProfile: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin)

        setupWindowInsets()
        initViews()
        setupListeners()

        // Default Fragment
        loadFragment(HomeFragment()) // Ganti AdminHomeFragment jika ada
        updateTabUI("home")
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // PERBAIKAN: Set bottom padding jadi 0 agar navigasi menempel ke bawah
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }

    private fun initViews() {
        btnHome = findViewById(R.id.btnHome)
        btnNews = findViewById(R.id.btnNews)
        btnHistory = findViewById(R.id.btnHistory)
        btnProfile = findViewById(R.id.btnProfile)
        btnFabPredict = findViewById(R.id.btnFabPredict)
        // btnLogout = findViewById(R.id.btnLogoutAdmin) <-- HAPUS INI

        imgHome = findViewById(R.id.imgHome)
        textHome = findViewById(R.id.textHome)
        imgNews = findViewById(R.id.imgNews)
        textNews = findViewById(R.id.textNews)
        imgHistory = findViewById(R.id.imgHistory)
        textHistory = findViewById(R.id.textHistory)
        imgProfile = findViewById(R.id.imgProfileTab)
        textProfile = findViewById(R.id.textProfile)
    }

    private fun setupListeners() {
        btnHome.setOnClickListener {
            loadFragment(HomeFragment())
            updateTabUI("home")
        }

        btnNews.setOnClickListener {
            loadFragment(NewsFragment())
            updateTabUI("news")
        }

        btnHistory.setOnClickListener {
            loadFragment(HistoryFragment())
            updateTabUI("history")
        }

        btnProfile.setOnClickListener {
            loadFragment(ProfileFragment())
            updateTabUI("profile")
        }

        btnFabPredict.setOnClickListener {
            val intent = Intent(this, PredictionActivity::class.java)
            startActivity(intent)
        }

        // KODE LOGOUT DIHAPUS DARI SINI (KARENA TOMBOLNYA HILANG)
        // Nanti logout lewat Fragment Profile saja
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun updateTabUI(selectedTab: String) {
        val inactiveColor = Color.parseColor("#757575")
        val activeColor = Color.parseColor("#2196F3")

        // Reset
        imgHome.setColorFilter(inactiveColor); textHome.setTextColor(inactiveColor)
        imgNews.setColorFilter(inactiveColor); textNews.setTextColor(inactiveColor)
        imgHistory.setColorFilter(inactiveColor); textHistory.setTextColor(inactiveColor)
        imgProfile.setColorFilter(inactiveColor); textProfile.setTextColor(inactiveColor)

        // Set Active
        when (selectedTab) {
            "home" -> { imgHome.setColorFilter(activeColor); textHome.setTextColor(activeColor) }
            "news" -> { imgNews.setColorFilter(activeColor); textNews.setTextColor(activeColor) }
            "history" -> { imgHistory.setColorFilter(activeColor); textHistory.setTextColor(activeColor) }
            "profile" -> { imgProfile.setColorFilter(activeColor); textProfile.setTextColor(activeColor) }
        }
    }
}