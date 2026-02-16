package com.barangay.pantal.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.databinding.ItemOfficialBinding
import com.barangay.pantal.models.Official
import com.squareup.picasso.Picasso

class OfficialAdapter(private val officials: List<Official>) :
    RecyclerView.Adapter<OfficialAdapter.OfficialViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            OfficialViewHolder {
        val binding = ItemOfficialBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return OfficialViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OfficialViewHolder, position: Int) {
        holder.bind(officials[position])
    }

    override fun getItemCount() = officials.size

    inner class OfficialViewHolder(private val binding: ItemOfficialBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(official: Official) {
            binding.officialName.text = official.name
            binding.officialPosition.text = official.position
            if (official.imageUrl.isNotEmpty()) {
                Picasso.get().load(official.imageUrl).into(binding.officialImage)
            }
        }
    }
}