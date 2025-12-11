package com.example.prediksipenyakit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Menghubungkan dengan layout fragment_home.xml yang baru dibuat
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
}