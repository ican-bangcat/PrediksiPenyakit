package com.example.prediksipenyakit

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // PENTING: Pastikan ini meng-inflate layout 'fragment_home' (Isi Dashboard)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- LOGIKA TOMBOL MULAI CEK ---
        // Kita cari tombol ID 'btnStartCheck' yang ada di dalam fragment_home.xml
        val btnStartCheck = view.findViewById<Button>(R.id.btnStartCheck)

        btnStartCheck.setOnClickListener {
            // Pindah ke Activity Prediksi
            val intent = Intent(requireContext(), PredictionActivity::class.java)
            startActivity(intent)
        }
    }
}