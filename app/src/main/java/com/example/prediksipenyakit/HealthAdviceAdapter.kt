package com.example.prediksipenyakit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.prediksipenyakit.databinding.ItemHealthAdviceBinding

class HealthAdviceAdapter(
    private val adviceList: List<String>
) : RecyclerView.Adapter<HealthAdviceAdapter.AdviceViewHolder>() {

    inner class AdviceViewHolder(private val binding: ItemHealthAdviceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(advice: String, position: Int) {
            binding.tvAdviceNumber.text = "${position + 1}"
            binding.tvAdviceText.text = advice
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdviceViewHolder {
        val binding = ItemHealthAdviceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AdviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdviceViewHolder, position: Int) {
        holder.bind(adviceList[position], position)
    }

    override fun getItemCount(): Int = adviceList.size
}