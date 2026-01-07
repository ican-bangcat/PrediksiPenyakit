package com.example.prediksipenyakit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private var historyList: List<PredictionHistoryModel>,
    private val onItemClick: (PredictionHistoryModel) -> Unit,
    private val onDeleteClick: (PredictionHistoryModel) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardHistory: MaterialCardView = view.findViewById(R.id.cardHistory)
        val viewIndicator: View = view.findViewById(R.id.viewIndicator)
        val badgeStatus: MaterialCardView = view.findViewById(R.id.badgeStatus)
        val tvStatusBadge: TextView = view.findViewById(R.id.tvStatusBadge)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvBP: TextView = view.findViewById(R.id.tvBP)
        val tvBMI: TextView = view.findViewById(R.id.tvBMI)
        val tvHeartRate: TextView = view.findViewById(R.id.tvHeartRate)

        // TAMBAHAN: Tombol Hapus (Pastikan ID di XML sudah @id/btnDelete)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyList[position]

        // --- 1. FORMAT TANGGAL ---
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        val formattedDate = try {
            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                .parse(item.createdAt ?: "")
            dateFormat.format(date ?: Date())
        } catch (e: Exception) {
            item.createdAt ?: "-"
        }

        holder.tvDate.text = formattedDate

        // --- 2. SET STATUS & WARNA ---
        if (item.predictionResult == 1) {
            // Kondisi: BERISIKO
            holder.viewIndicator.setBackgroundColor(
                holder.itemView.context.getColor(android.R.color.holo_red_light)
            )
            holder.badgeStatus.setCardBackgroundColor(
                holder.itemView.context.getColor(R.color.badge_risk_bg)
            )
            holder.tvStatusBadge.text = "● Berisiko"
            holder.tvStatusBadge.setTextColor(
                holder.itemView.context.getColor(R.color.badge_risk_text)
            )
            holder.tvStatus.text = "Kondisi Perlu Perhatian"
            holder.tvStatus.setTextColor(
                holder.itemView.context.getColor(android.R.color.holo_red_dark)
            )
        } else {
            // Kondisi: SEHAT
            holder.viewIndicator.setBackgroundColor(
                holder.itemView.context.getColor(android.R.color.holo_green_light)
            )
            holder.badgeStatus.setCardBackgroundColor(
                holder.itemView.context.getColor(R.color.badge_healthy_bg)
            )
            holder.tvStatusBadge.text = "● Sehat"
            holder.tvStatusBadge.setTextColor(
                holder.itemView.context.getColor(R.color.badge_healthy_text)
            )
            holder.tvStatus.text = "Kondisi Sehat"
            holder.tvStatus.setTextColor(
                holder.itemView.context.getColor(android.R.color.holo_green_dark)
            )
        }

        //  3. TAMPILKAN METRIK KESEHATAN ---
        holder.tvBP.text = "${item.systolicBp}/${item.diastolicBp}"
        holder.tvBMI.text = String.format("%.1f", item.bmi)
        holder.tvHeartRate.text = "${item.heartRate} bpm"

        // 4. CLICK LISTENERS ---

        // Klik Kartu -> Buka Detail
        holder.cardHistory.setOnClickListener {
            onItemClick(item)
        }

        // Klik Sampah -> Hapus Data (TAMBAHAN)
        holder.btnDelete.setOnClickListener {
            onDeleteClick(item)
        }
    }

    override fun getItemCount(): Int = historyList.size

    fun updateData(newList: List<PredictionHistoryModel>) {
        historyList = newList
        notifyDataSetChanged()
    }
}