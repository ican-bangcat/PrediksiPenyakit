package com.example.prediksipenyakit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class NewsDetailHostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_container)

        // Mencegah fragment dibuat double saat rotasi layar
        if (savedInstanceState == null) {
            // 1. Ambil data yang dikirim dari ResultActivity
            val title = intent.getStringExtra("TITLE")
            val content = intent.getStringExtra("CONTENT")
            val category = intent.getStringExtra("CATEGORY")
            val date = intent.getStringExtra("DATE")
            val imageUrl = intent.getStringExtra("IMAGE_URL")

            // 2. Siapkan Fragment
            val fragment = DetailNewsFragment()

            // 3. Masukkan data ke Bundle (Sesuai kunci di Fragment kamu)
            val bundle = Bundle().apply {
                putString("TITLE", title)
                putString("CONTENT", content)
                putString("CATEGORY", category)
                putString("DATE", date)
                putString("IMAGE_URL", imageUrl)
            }
            fragment.arguments = bundle

            // 4. Tampilkan Fragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
        }
    }
}