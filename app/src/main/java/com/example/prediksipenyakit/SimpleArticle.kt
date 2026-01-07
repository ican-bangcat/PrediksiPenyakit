package com.example.prediksipenyakit

// Model data sederhana untuk artikel rekomendasi (Lokal)
data class SimpleArticle(
    val title: String,
    val category: String,
    val imageRes: Int // Menggunakan ID Drawable (contoh: R.drawable.gambar_lari)
)