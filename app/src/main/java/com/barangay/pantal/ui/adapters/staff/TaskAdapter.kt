package com.barangay.pantal.ui.adapters.staff

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.databinding.ItemTaskBinding
import com.barangay.pantal.model.Request
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskAdapter(private val tasks: List<Request>) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount() = tasks.size

    inner class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Request) {
            binding.taskTitle.text = task.type
            binding.taskSubtitle.text = task.name
            
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.taskDate.text = task.timestamp?.let { sdf.format(Date(it)) } ?: "N/A"

            binding.taskStatus.text = task.status

            // Set status color based on status
            // You can create a helper function for this
        }
    }
}
