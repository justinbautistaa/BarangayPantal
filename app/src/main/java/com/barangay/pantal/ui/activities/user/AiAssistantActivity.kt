package com.barangay.pantal.ui.activities.user

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.barangay.pantal.databinding.ActivityAiAssistantBinding
import com.barangay.pantal.network.DeepSeekMessage
import com.barangay.pantal.network.DeepSeekRequest
import com.barangay.pantal.network.DeepSeekService
import com.barangay.pantal.ui.adapters.MessageAdapter
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AiAssistantActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAiAssistantBinding
    private lateinit var adapter: MessageAdapter
    // We keep a separate list for the API which includes the system message
    private val apiMessages = mutableListOf<DeepSeekMessage>()
    // This list is for the UI (we don't show the system message to the user)
    private val uiMessages = mutableListOf<DeepSeekMessage>()

    private val deepSeekService: DeepSeekService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.deepseek.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DeepSeekService::class.java)
    }

    private val apiKey = "Bearer sk-a61076d9ebe64317b243cd6261349ca9"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiAssistantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        // 1. Set the AI's Identity (Role: "system" - NOT "developer")
        apiMessages.add(DeepSeekMessage(
            role = "system", 
            content = "You are the Barangay Pantal AI Assistant. You help residents with inquiries about clearances, community services, and local news in Barangay Pantal, Dagupan City. Be helpful, professional, and friendly."
        ))

        binding.sendButton.setOnClickListener {
            val content = binding.messageEditText.text.toString().trim()
            if (content.isNotEmpty()) {
                sendMessage(content)
                binding.messageEditText.text.clear()
            }
        }
        
        // Initial bot greeting
        addBotMessage("Hello! I am your Barangay Pantal AI Assistant. How can I help you today?")
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter(uiMessages)
        binding.chatRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatRecyclerView.adapter = adapter
    }

    private fun sendMessage(content: String) {
        val userMessage = DeepSeekMessage(role = "user", content = content)
        
        // Add to both lists
        uiMessages.add(userMessage)
        apiMessages.add(userMessage)
        
        adapter.notifyItemInserted(uiMessages.size - 1)
        binding.chatRecyclerView.scrollToPosition(uiMessages.size - 1)

        lifecycleScope.launch {
            try {
                binding.sendButton.isEnabled = false
                val request = DeepSeekRequest(
                    messages = apiMessages
                )
                val response = deepSeekService.getChatCompletion(apiKey, request)
                
                if (response.isSuccessful) {
                    val botContent = response.body()?.choices?.firstOrNull()?.message?.content
                    if (botContent != null) {
                        addBotMessage(botContent)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@AiAssistantActivity, "API Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AiAssistantActivity, "Connection error. Check internet.", Toast.LENGTH_SHORT).show()
            } finally {
                binding.sendButton.isEnabled = true
            }
        }
    }

    private fun addBotMessage(content: String) {
        val botMessage = DeepSeekMessage(role = "assistant", content = content)
        uiMessages.add(botMessage)
        apiMessages.add(botMessage)
        adapter.notifyItemInserted(uiMessages.size - 1)
        binding.chatRecyclerView.scrollToPosition(uiMessages.size - 1)
    }
}
