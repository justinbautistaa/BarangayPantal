package com.barangay.pantal.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.barangay.pantal.R
import com.barangay.pantal.network.DeepSeekMessage

class MessageAdapter(private val messages: MutableList<DeepSeekMessage>) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val messageText: TextView = view.findViewById(R.id.message_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layout = if (viewType == 0) R.layout.item_message_user else R.layout.item_message_bot
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.messageText.text = messages[position].content
    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].role == "user") 0 else 1
    }

    fun addMessage(message: DeepSeekMessage) {
        messages.add(message)
        notifyItemInserted(messages.size - 1)
    }
}
