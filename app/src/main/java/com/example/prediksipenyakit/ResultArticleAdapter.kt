package com.example.prediksipenyakit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Pastikan library Glide sudah ada

class ResultArticleAdapter(
    private var articles: List<ArticleModel>, // Menggunakan Model Asli dari Supabase
    private val onItemClick: (ArticleModel) -> Unit // Fungsi ketika diklik
) : RecyclerView.Adapter<ResultArticleAdapter.ArticleViewHolder>() {

    // Menghubungkan ID di XML dengan Variable Kotlin
    inner class ArticleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgCover: ImageView = view.findViewById(R.id.imgArticleThumb)
        val tvCategory: TextView = view.findViewById(R.id.tvArticleCategory)
        val tvTitle: TextView = view.findViewById(R.id.tvArticleTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_article, parent, false)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val item = articles[position]

        // 1. Set Text
        holder.tvTitle.text = item.title
        holder.tvCategory.text = item.category

        // 2. Load Gambar (URL) dari Supabase menggunakan Glide
        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.ic_launcher_background) // Gambar saat loading (bisa diganti)
            .error(R.drawable.ic_launcher_background) // Gambar jika error/link mati
            .centerCrop() // Agar gambar penuh dan rapi
            .into(holder.imgCover)

        // 3. Listener Klik (Saat kartu ditekan)
        // Kita pakai 'holder.itemView' (ini merujuk ke MaterialCardView pembungkus utama)
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = articles.size

    // Fungsi untuk update data dari Activity (misal setelah loading selesai)
    fun updateData(newArticles: List<ArticleModel>) {
        articles = newArticles
        notifyDataSetChanged()
    }
}