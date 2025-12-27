package com.example.prediksipenyakit

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {
    private const val SUPABASE_URL = "https://xzhxzwpneczqbnrrziyv.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inh6aHh6d3BuZWN6cWJucnJ6aXl2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjY4Mzk2MTUsImV4cCI6MjA4MjQxNTYxNX0.NGjwnn7LaJK69m3_izzMcS4tokp44FJcN3DNwfUCtbs"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        // Pasang fitur-fitur yang mau kita pakai
        install(Auth)      // Buat Login/Register
        install(Postgrest) // Buat Database
        install(Storage)   // Buat Gambar
    }
}