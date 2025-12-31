package com.example.prediksipenyakit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

// PERUBAHAN 1: Tambahkan parameter kedua 'onItemClick' di constructor
class HomeArticleAdapter(
    private val articleList: List<ArticleModel>,
    private val onItemClick: (ArticleModel) -> Unit
) : RecyclerView.Adapter<HomeArticleAdapter.ArticleViewHolder>() {

    class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val imgArticle: ImageView = itemView.findViewById(R.id.imgArticle)
        val tvTitle: TextView = itemView.findViewById(R.id.tvArticleTitle)
        val tvCategory: TextView = itemView.findViewById(R.id.tvArticleCategory)
//        val tvDate: TextView = itemView.findViewById(R.id.tvArticleDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        // Pastikan nama layout item kamu benar (misal: item_article atau item_home_article)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_article, parent, false)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = articleList[position]

        holder.tvTitle.text = article.title
        holder.tvCategory.text = article.category

        // Format tanggal sederhana
//        holder.tvDate.text = article.publishedAt?.take(10) ?: "Baru saja"

        // Load gambar pakai Coil
//        holder.imgArticle.load(article.imageUrl) {
//            placeholder(R.drawable.ic_newspaper)
//            error(R.drawable.ic_newspaper)
//        }

        // PERUBAHAN 2: Pasang Listener Klik pada Item
        holder.itemView.setOnClickListener {
            onItemClick(article) // Panggil fungsi yang dikirim dari HomeFragment
        }
    }

    override fun getItemCount(): Int = articleList.size
}