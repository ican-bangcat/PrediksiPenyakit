package com.example.prediksipenyakit

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class AddEditNewsFragment : Fragment() {

    private lateinit var etTitle: TextInputEditText
    private lateinit var etCategory: AutoCompleteTextView
    private lateinit var etContent: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnBack: ImageView
    private lateinit var tvPageTitle: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvLoadingText: TextView

    // Image Upload Views
    private lateinit var btnSelectImage: CardView
    private lateinit var imgPreview: ImageView
    private lateinit var layoutPlaceholder: LinearLayout
    private lateinit var overlayChangeImage: View
    private lateinit var tvChangeImage: TextView

    private var articleId: String? = null
    private var selectedImageUri: Uri? = null
    private var currentImageUrl: String? = null

    // Setup Image Picker
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            showImagePreview(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_edit_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sembunyikan Bottom Navigation saat di halaman tambah/edit artikel
        activity?.findViewById<View>(R.id.bottomNavContainer)?.visibility = View.GONE
        activity?.findViewById<View>(R.id.btnFabPredict)?.visibility = View.GONE

        // Init Views
        etTitle = view.findViewById(R.id.etTitle)
        etCategory = view.findViewById(R.id.etCategory)
        etContent = view.findViewById(R.id.etContent)
        btnSave = view.findViewById(R.id.btnSaveNews)
        btnBack = view.findViewById(R.id.btnBack)
        tvPageTitle = view.findViewById(R.id.tvPageTitle)
        progressBar = view.findViewById(R.id.progressBar)
        tvLoadingText = view.findViewById(R.id.tvLoadingText)

        btnSelectImage = view.findViewById(R.id.btnSelectImage)
        imgPreview = view.findViewById(R.id.imgPreview)
        layoutPlaceholder = view.findViewById(R.id.layoutPlaceholder)
        overlayChangeImage = view.findViewById(R.id.overlayChangeImage)
        tvChangeImage = view.findViewById(R.id.tvChangeImage)

        // Setup Dropdown
        val categories = arrayOf(
            "Kesehatan Jantung",
            "Nutrisi & Diet",
            "Aktivitas Fisik",
            "Tidur & Istirahat",
            "Gaya Hidup",
            "Info Medis",
            "Umum"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        etCategory.setAdapter(adapter)

        // Cek Arguments (Edit atau Tambah)
        articleId = arguments?.getString("ARTICLE_ID")

        if (articleId != null) {
            // MODE EDIT
            tvPageTitle.text = "Edit Artikel"
            btnSave.text = "Update Artikel"

            etTitle.setText(arguments?.getString("TITLE"))
            etContent.setText(arguments?.getString("CONTENT"))
            etCategory.setText(arguments?.getString("CATEGORY"), false)

            currentImageUrl = arguments?.getString("IMAGE_URL")
            if (!currentImageUrl.isNullOrEmpty()) {
                showImagePreview(currentImageUrl!!)
            }
        } else {
            // MODE TAMBAH - Tampilkan placeholder
            showPlaceholder()
        }

        // Listener
        btnBack.setOnClickListener {
            // Tampilkan kembali Bottom Navigation saat kembali
            activity?.findViewById<View>(R.id.bottomNavContainer)?.visibility = View.VISIBLE
            activity?.findViewById<View>(R.id.btnFabPredict)?.visibility = View.VISIBLE
            parentFragmentManager.popBackStack()
        }

        // Klik Kotak Gambar -> Buka Galeri
        btnSelectImage.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        btnSave.setOnClickListener {
            if (validateInput()) {
                startUploadAndSave()
            }
        }
    }

    /**
     * Tampilkan placeholder saat belum ada gambar
     */
    private fun showPlaceholder() {
        layoutPlaceholder.visibility = View.VISIBLE
        imgPreview.visibility = View.GONE
        overlayChangeImage.visibility = View.GONE
        tvChangeImage.visibility = View.GONE
    }

    /**
     * Tampilkan preview gambar (dari Uri atau URL)
     */
    private fun showImagePreview(imageSource: Any) {
        layoutPlaceholder.visibility = View.GONE
        imgPreview.visibility = View.VISIBLE
        overlayChangeImage.visibility = View.GONE
        tvChangeImage.visibility = View.GONE

        // Load gambar menggunakan Coil
        imgPreview.load(imageSource) {
            crossfade(true)
            placeholder(R.drawable.ic_add_photo)
            error(R.drawable.ic_add_photo)
        }
    }

    private fun validateInput(): Boolean {
        if (etTitle.text.isNullOrEmpty()) {
            etTitle.error = "Judul wajib diisi"
            etTitle.requestFocus()
            return false
        }
        if (etContent.text.isNullOrEmpty()) {
            etContent.error = "Isi wajib diisi"
            etContent.requestFocus()
            return false
        }
        // Validasi gambar
        if (selectedImageUri == null && currentImageUrl == null) {
            Toast.makeText(context, "Mohon pilih gambar artikel", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun startUploadAndSave() {
        showLoading(true)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val currentUser = SupabaseClient.client.auth.currentUserOrNull()
                val authorId = currentUser?.id ?: run {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        Toast.makeText(context, "User tidak terautentikasi", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                var finalImageUrl = currentImageUrl // Default pakai gambar lama

                // 1. JIKA ADA GAMBAR BARU DIPILIH -> UPLOAD KE SUPABASE
                if (selectedImageUri != null) {
                    withContext(Dispatchers.Main) {
                        tvLoadingText.text = "Mengupload gambar..."
                    }

                    val bucket = SupabaseClient.client.storage.from("news-images")

                    // Baca file dari Uri menjadi ByteArray
                    val imageBytes = requireContext().contentResolver.openInputStream(selectedImageUri!!)?.use {
                        it.readBytes()
                    }

                    if (imageBytes != null) {
                        val fileName = "news_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"

                        // Upload
                        bucket.upload(fileName, imageBytes)

                        // Get Public URL
                        finalImageUrl = bucket.publicUrl(fileName)

                        Log.d("UPLOAD_SUCCESS", "Image uploaded: $finalImageUrl")
                    } else {
                        throw Exception("Gagal membaca file gambar")
                    }
                }

                // 2. SIMPAN DATA ARTIKEL KE DATABASE
                withContext(Dispatchers.Main) {
                    tvLoadingText.text = "Menyimpan artikel..."
                }

                val title = etTitle.text.toString().trim()
                val content = etContent.text.toString().trim()
                val category = etCategory.text.toString().trim()

                if (articleId == null) {
                    // INSERT - Artikel Baru
                    val newArticle = ArticleModel(
                        authorId = authorId,
                        title = title,
                        content = content,
                        category = category,
                        imageUrl = finalImageUrl
                    )
                    SupabaseClient.client.from("articles").insert(newArticle)

                    Log.d("INSERT_SUCCESS", "Article created successfully")
                } else {
                    // UPDATE - Edit Artikel
                    SupabaseClient.client.from("articles").update(
                        mapOf(
                            "title" to title,
                            "content" to content,
                            "category" to category,
                            "image_url" to finalImageUrl,
                            "updated_at" to "now()" // Opsional: update timestamp
                        )
                    ) {
                        filter {
                            eq("article_id", articleId!!)
                        }
                    }

                    Log.d("UPDATE_SUCCESS", "Article updated successfully")
                }

                withContext(Dispatchers.Main) {
                    showLoading(false)
                    val message = if (articleId == null) "Artikel berhasil dipublikasikan!" else "Artikel berhasil diupdate!"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                    // Tampilkan kembali Bottom Navigation
                    activity?.findViewById<View>(R.id.bottomNavContainer)?.visibility = View.VISIBLE
                    activity?.findViewById<View>(R.id.btnFabPredict)?.visibility = View.VISIBLE

                    // Kembali ke halaman sebelumnya
                    parentFragmentManager.popBackStack()
                }

            } catch (e: Exception) {
                Log.e("UPLOAD_ERROR", "Error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    val errorMessage = when {
                        e.message?.contains("storage") == true -> "Gagal mengupload gambar"
                        e.message?.contains("insert") == true || e.message?.contains("update") == true -> "Gagal menyimpan artikel"
                        else -> "Terjadi kesalahan: ${e.message}"
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
            tvLoadingText.visibility = View.VISIBLE
            tvLoadingText.text = "Memproses..."
            btnSave.isEnabled = false
            btnSelectImage.isEnabled = false
            etTitle.isEnabled = false
            etCategory.isEnabled = false
            etContent.isEnabled = false
        } else {
            progressBar.visibility = View.GONE
            tvLoadingText.visibility = View.GONE
            btnSave.isEnabled = true
            btnSelectImage.isEnabled = true
            etTitle.isEnabled = true
            etCategory.isEnabled = true
            etContent.isEnabled = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Pastikan Bottom Navigation ditampilkan kembali saat fragment dihancurkan
        activity?.findViewById<View>(R.id.bottomNavContainer)?.visibility = View.VISIBLE
        activity?.findViewById<View>(R.id.btnFabPredict)?.visibility = View.VISIBLE
    }
}