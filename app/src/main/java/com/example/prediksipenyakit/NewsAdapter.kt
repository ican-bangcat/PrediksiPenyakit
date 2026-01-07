package com.example.prediksipenyakit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

// import com.bumptech.glide.Glide // Uncomment jika pakai Glide

class NewsAdapter(
    private var newsList: List<ArticleModel>,
    private val isAdmin: Boolean,
    private val onEditClick: (ArticleModel) -> Unit,
    private val onDeleteClick: (ArticleModel) -> Unit,
    private val onItemClick: (ArticleModel) -> Unit
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    inner class NewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvTitle: TextView = view.findViewById(R.id.tvNewsTitle)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val imgNews: ImageView = view.findViewById(R.id.imgNewsThumbnail)

        // Tombol
        val btnReadMore: ImageView = view.findViewById(R.id.btnReadMore)
        val adminControls: LinearLayout = view.findViewById(R.id.adminControls)
        val btnEdit: ImageView = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val item = newsList[position]

        holder.tvTitle.text = item.title
        holder.tvCategory.text = item.category?.uppercase() ?: "UMUM"
        holder.tvDate.text = item.publishedAt?.take(10) ?: "-"

        // BAGIAN BARU: LOAD GAMBAR DENGAN COIL
        // Pastikan import coil.load di bagian atas file
        if (!item.imageUrl.isNullOrEmpty()) {
            holder.imgNews.load(item.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_newspaper) // Gambar loading/default
                error(R.drawable.ic_newspaper)       // Gambar jika error/link rusak

                // Opsional: Bikin gambar kotak jadi agak tumpul (Rounded)
                transformations(coil.transform.RoundedCornersTransformation(16f))
            }
        } else {
            // Jika tidak ada URL gambar, pakai gambar default
            holder.imgNews.setImageResource(R.drawable.ic_newspaper)
        }
        // ---------------------------------------------

        // LOGIKA ADMIN (Tetap sama)
        if (isAdmin) {
            holder.adminControls.visibility = View.VISIBLE
            holder.btnReadMore.visibility = View.GONE
        } else {
            holder.adminControls.visibility = View.GONE
            holder.btnReadMore.visibility = View.VISIBLE
        }

        holder.btnEdit.setOnClickListener { onEditClick(item) }
        holder.btnDelete.setOnClickListener { onDeleteClick(item) }
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = newsList.size

    fun updateData(newList: List<ArticleModel>) {
        newsList = newList
        notifyDataSetChanged()
    }
}