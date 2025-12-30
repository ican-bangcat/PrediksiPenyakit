package com.example.prediksipenyakit

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArticleModel(
    @SerialName("article_id") val id: String? = null,
    @SerialName("author_id") val authorId: String? = null,
    val title: String,
    val content: String,
    val category: String? = "Umum",
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("published_at") val publishedAt: String? = null,

    // --- TAMBAHAN BARU ---
    @SerialName("updated_at") val updatedAt: String? = null
)