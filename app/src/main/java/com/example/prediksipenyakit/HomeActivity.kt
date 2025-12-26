package com.example.prediksipenyakit

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class HomeActivity : AppCompatActivity() {

    // Deklarasi variabel komponen UI agar bisa diakses global di kelas ini
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
        setContentView(R.layout.activity_home)

        // 1. Inisialisasi Komponen View (Icon & Text)
        imgHome = findViewById(R.id.imgHome)
        textHome = findViewById(R.id.textHome)
        imgNews = findViewById(R.id.imgNews)
        textNews = findViewById(R.id.textNews)
        imgHistory = findViewById(R.id.imgHistory)
        textHistory = findViewById(R.id.textHistory)
        imgProfile = findViewById(R.id.imgProfileTab)
        textProfile = findViewById(R.id.textProfile)

        // Inisialisasi Tombol Container
        val btnHome = findViewById<LinearLayout>(R.id.btnHome)
        val btnNews = findViewById<LinearLayout>(R.id.btnNews)
        val btnHistory = findViewById<LinearLayout>(R.id.btnHistory)
        val btnProfile = findViewById<LinearLayout>(R.id.btnProfile)
        val btnPredict = findViewById<CardView>(R.id.btnFabPredict)

        // 2. Set Default: Load Home & Warnai Home jadi Biru
        loadFragment(HomeFragment())
        updateTabUI("home")

        // 3. Listener Klik
        btnHome.setOnClickListener {
            loadFragment(HomeFragment())
            updateTabUI("home") // Ubah warna jadi biru
        }

        btnNews.setOnClickListener {
            loadFragment(NewsFragment())
            updateTabUI("news") // Ubah warna jadi biru
        }

        btnHistory.setOnClickListener {
            loadFragment(HistoryFragment())
            updateTabUI("history") // Ubah warna jadi biru
        }

        btnProfile.setOnClickListener {
            loadFragment(ProfileFragment())
            updateTabUI("profile") // Agar icon profile jadi biru
        }

        btnPredict.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    // --- LOGIKA GANTI WARNA ICON ---
    private fun updateTabUI(activeTab: String) {
        // Warna Biru (Active) dan Abu-abu (Inactive)
        val colorActive = Color.parseColor("#2196F3")
        val colorInactive = Color.parseColor("#757575")

        // Reset semua ke abu-abu dulu
        imgHome.setColorFilter(colorInactive)
        textHome.setTextColor(colorInactive)
        imgNews.setColorFilter(colorInactive)
        textNews.setTextColor(colorInactive)
        imgHistory.setColorFilter(colorInactive)
        textHistory.setTextColor(colorInactive)
        imgProfile.setColorFilter(colorInactive)
        textProfile.setTextColor(colorInactive)

        // Set warna biru hanya untuk yang dipilih
        when (activeTab) {
            "home" -> {
                imgHome.setColorFilter(colorActive)
                textHome.setTextColor(colorActive)
            }
            "news" -> {
                imgNews.setColorFilter(colorActive)
                textNews.setTextColor(colorActive)
            }
            "history" -> {
                imgHistory.setColorFilter(colorActive)
                textHistory.setTextColor(colorActive)
            }
            "profile" -> {
                imgProfile.setColorFilter(colorActive)
                textProfile.setTextColor(colorActive)
            }
        }
    }
}