package com.example.prediksipenyakit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
// import androidx.recyclerview.widget.LinearLayoutManager
// import androidx.recyclerview.widget.RecyclerView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Inisialisasi RecyclerView untuk Blog (Optional: Perlu buat Adapter jika ingin dinamis)
        // val rvBlog = findViewById<RecyclerView>(R.id.rvBlog)
        // rvBlog.layoutManager = LinearLayoutManager(this)
        // rvBlog.adapter = BlogAdapter(...)

        // --- LOGIKA UTAMA ---

        // Cari tombol floating action button (Tombol Tengah)
        val btnPredict = findViewById<CardView>(R.id.btnFabPredict)

        // Ketika tombol tengah diklik -> Pindah ke MainActivity (Halaman Prediksi)
        btnPredict.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Setup tombol navbar lain (contoh saja)
        val btnNews = findViewById<LinearLayout>(R.id.btnNews)
        btnNews.setOnClickListener {
            // Logika pindah ke halaman news
        }
    }
}