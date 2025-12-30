package com.example.prediksipenyakit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import coil.load // Pastikan import coil

class DetailNewsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Init View
        val imgHeader: ImageView = view.findViewById(R.id.imgDetailNews)
        val tvCategory: TextView = view.findViewById(R.id.tvDetailCategory)
        val tvDate: TextView = view.findViewById(R.id.tvDetailDate)
        val tvTitle: TextView = view.findViewById(R.id.tvDetailTitle)
        val tvContent: TextView = view.findViewById(R.id.tvDetailContent)

        // UBAH BAGIAN INI: Cari ImageView btnBack, bukan Toolbar
        val btnBack: ImageView = view.findViewById(R.id.btnBack)

        // 2. Ambil Data (Sama seperti sebelumnya)
        val title = arguments?.getString("TITLE") ?: ""
        val content = arguments?.getString("CONTENT") ?: ""
        val category = arguments?.getString("CATEGORY") ?: "Umum"
        val date = arguments?.getString("DATE") ?: ""
        val imageUrl = arguments?.getString("IMAGE_URL")

        // 3. Set Data
        tvTitle.text = title
        tvContent.text = content
        tvCategory.text = category.uppercase()
        tvDate.text = date.take(10)

        if (!imageUrl.isNullOrEmpty()) {
            imgHeader.load(imageUrl) {
                placeholder(R.drawable.ic_newspaper)
                error(R.drawable.ic_newspaper)
            }
        }

        // 4. Tombol Back (Logic Baru)
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
}