package com.barangay.pantal.ui.adapters.user

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.databinding.ItemProgramServiceBinding
import com.barangay.pantal.model.ProgramService

class ProgramServiceAdapter(
    private var programs: List<ProgramService>,
    private val onItemClick: (ProgramService) -> Unit
) : RecyclerView.Adapter<ProgramServiceAdapter.ProgramServiceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramServiceViewHolder {
        val binding = ItemProgramServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProgramServiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProgramServiceViewHolder, position: Int) {
        holder.bind(programs[position])
    }

    override fun getItemCount(): Int = programs.size

    fun updateData(newPrograms: List<ProgramService>) {
        programs = newPrograms
        notifyDataSetChanged()
    }

    inner class ProgramServiceViewHolder(
        private val binding: ItemProgramServiceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(program: ProgramService) {
            binding.tvProgramTitle.text = program.title
            binding.tvProgramCategory.text = program.category
            binding.tvProgramDescription.text = program.description
            binding.tvProgramVenue.text = program.venue
            binding.tvProgramIcon.text = program.title.take(1).uppercase()

            binding.root.setOnClickListener { onItemClick(program) }
            binding.btnViewDetails.setOnClickListener { onItemClick(program) }
        }
    }
}
