package com.example.prediksipenyakit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.RoundedCornersTransformation

class HomeArticleAdapter(
    private val articleList: List<ArticleModel>,
    private val onItemClick: (ArticleModel) -> Unit
) : RecyclerView.Adapter<HomeArticleAdapter.ArticleViewHolder>() {

    class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 1. UNCOMMENT: Inisialisasi ImageView
        // Pastikan ID di layout item_article.xml kamu bernama "imgArticle"
        val imgArticle: ImageView = itemView.findViewById(R.id.imgArticleThumb)

        val tvTitle: TextView = itemView.findViewById(R.id.tvArticleTitle)
        val tvCategory: TextView = itemView.findViewById(R.id.tvArticleCategory)
        // val tvDate: TextView = itemView.findViewById(R.id.tvArticleDate) // Opsional jika mau dipakai
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_article, parent, false)
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = articleList[position]

        holder.tvTitle.text = article.title
        holder.tvCategory.text = article.category

        // 2. UNCOMMENT & UPDATE: Load gambar pakai Coil
        holder.imgArticle.load(article.imageUrl) {
            // Efek memudar saat gambar muncul (biar halus)
            crossfade(true)
            crossfade(500)

            // Membuat sudut gambar melengkung (sesuai desain card)
            transformations(RoundedCornersTransformation(12f))

            // Gambar sementara jika loading atau error
            // Pastikan kamu punya drawable 'ic_newspaper' atau ganti dengan icon lain
            placeholder(R.drawable.ic_newspaper)
            error(R.drawable.ic_newspaper)
        }

        // 3. Listener Klik
        holder.itemView.setOnClickListener {
            onItemClick(article)
        }
    }

    override fun getItemCount(): Int = articleList.size
}