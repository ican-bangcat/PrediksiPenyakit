package com.example.prediksipenyakit

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewsFragment : Fragment() {

    private lateinit var rvNews: RecyclerView
    private lateinit var adapter: NewsAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var etSearch: EditText
    private lateinit var fabAddNews: FloatingActionButton

    // --- UPDATE: Variabel Chips Sesuai XML Baru ---
    private lateinit var chipSemua: TextView
    private lateinit var chipJantung: TextView
    private lateinit var chipNutrisi: TextView
    private lateinit var chipFisik: TextView
    private lateinit var chipTidur: TextView
    private lateinit var chipUmum: TextView

    private var allArticles: List<ArticleModel> = emptyList()
    private var isAdmin = false
    private var currentCategory = "Semua"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Init Views
        rvNews = view.findViewById(R.id.rvNews)
        progressBar = view.findViewById(R.id.progressBarNews)
        etSearch = view.findViewById(R.id.etSearch)
        fabAddNews = view.findViewById(R.id.fabAddNews)

        // --- UPDATE: FindViewById Sesuai ID XML Baru ---
        chipSemua = view.findViewById(R.id.chipAll)
        chipJantung = view.findViewById(R.id.chipJantung)
        chipNutrisi = view.findViewById(R.id.chipNutrisi)
        chipFisik = view.findViewById(R.id.chipFisik)
        chipTidur = view.findViewById(R.id.chipTidur)
        chipUmum = view.findViewById(R.id.chipUmum)

        rvNews.layoutManager = LinearLayoutManager(context)

        // Setup Listeners
        setupChipListeners()
        setupSearchListener()

        // Load Data
        checkRoleAndFetchNews()

        // Admin Add Button
        fabAddNews.setOnClickListener {
            // Pindah ke AddEditNewsFragment (Mode Tambah -> tanpa ID)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AddEditNewsFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    // --- UPDATE: Logika Filter Kategori Baru ---
    private fun setupChipListeners() {
        // Masukkan semua chip ke dalam list agar mudah diatur warnanya
        val chips = listOf(chipSemua, chipJantung, chipNutrisi, chipFisik, chipTidur, chipUmum)

        fun updateChipUI(selectedChip: TextView) {
            chips.forEach { chip ->
                if (chip == selectedChip) {
                    chip.setBackgroundResource(R.drawable.bg_chip_active)
                    chip.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                } else {
                    chip.setBackgroundResource(R.drawable.bg_chip_inactive)
                    chip.setTextColor(android.graphics.Color.parseColor("#64748B")) // Warna abu-abu
                }
            }
        }

        // Set Listener untuk masing-masing chip
        chipSemua.setOnClickListener {
            updateChipUI(chipSemua)
            filterNews("Semua")
        }
        chipJantung.setOnClickListener {
            updateChipUI(chipJantung)
            filterNews("Kesehatan Jantung")
        }
        chipNutrisi.setOnClickListener {
            updateChipUI(chipNutrisi)
            filterNews("Nutrisi & Diet")
        }
        chipFisik.setOnClickListener {
            updateChipUI(chipFisik)
            filterNews("Aktivitas Fisik")
        }
        chipTidur.setOnClickListener {
            updateChipUI(chipTidur)
            filterNews("Tidur & Istirahat")
        }
        chipUmum.setOnClickListener {
            updateChipUI(chipUmum)
            filterNews("Umum")
        }
    }

    private fun setupSearchListener() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterNews(currentCategory, s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun checkRoleAndFetchNews() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // 1. Cek Role
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                val userId = currentUser?.id

                if (userId != null) {
                    val profile = SupabaseClient.client.from("profiles")
                        .select { filter { eq("id", userId) } }
                        .decodeSingleOrNull<ProfileFragment.UserProfileData>()

                    // Logic simpel: jika role admin -> true
                    isAdmin = profile?.role == "admin"
                }

                // 2. Fetch Berita
                val result = SupabaseClient.client.from("articles")
                    .select { order("published_at", order = Order.DESCENDING) }
                    .decodeList<ArticleModel>()

                allArticles = result

                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE

                    // Tampilkan FAB hanya jika Admin
                    fabAddNews.visibility = if (isAdmin) View.VISIBLE else View.GONE

                    // Setup Adapter
                    adapter = NewsAdapter(allArticles, isAdmin,
                        onEditClick = { item ->
                            val fragment = AddEditNewsFragment().apply {
                                arguments = Bundle().apply {
                                    putString("ARTICLE_ID", item.id)
                                    putString("TITLE", item.title)
                                    putString("CONTENT", item.content)
                                    putString("CATEGORY", item.category)
                                    putString("IMAGE_URL", item.imageUrl)
                                }
                            }
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragmentContainer, fragment)
                                .addToBackStack(null)
                                .commit()
                        },
                        onDeleteClick = { item ->
                            deleteArticle(item)
                        },
                        onItemClick = { item ->
                            // Buka Halaman Detail
                            val fragment = DetailNewsFragment().apply {
                                arguments = Bundle().apply {
                                    putString("TITLE", item.title)
                                    putString("CONTENT", item.content)
                                    putString("CATEGORY", item.category)
                                    putString("DATE", item.publishedAt)
                                    putString("IMAGE_URL", item.imageUrl)
                                }
                            }

                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragmentContainer, fragment)
                                .addToBackStack(null) // Agar bisa di-back
                                .commit()
                        }
                    )
                    rvNews.adapter = adapter
                }
            } catch (e: Exception) {
                Log.e("NEWS", "Error: ${e.message}")
                withContext(Dispatchers.Main) { progressBar.visibility = View.GONE }
            }
        }
    }

    private fun filterNews(category: String, query: String = etSearch.text.toString()) {
        currentCategory = category

        val filteredList = allArticles.filter { article ->
            val matchCategory = if (category == "Semua") true else article.category.equals(category, ignoreCase = true)
            val matchSearch = article.title.contains(query, ignoreCase = true) || article.content.contains(query, ignoreCase = true)

            matchCategory && matchSearch
        }

        if (::adapter.isInitialized) {
            adapter.updateData(filteredList)
        }
    }

    private fun deleteArticle(item: ArticleModel) {
        // Logika Hapus Data
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (item.id != null) {
                    SupabaseClient.client.from("articles").delete {
                        filter { eq("article_id", item.id) }
                    }
                    // Refresh data setelah hapus
                    checkRoleAndFetchNews()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Berita dihapus", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch(e: Exception) {
                Log.e("DELETE", "Error: ${e.message}")
            }
        }
    }
}