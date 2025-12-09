package com.example.inasafe

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import java.util.Calendar

@Serializable
data class ChatMessage(
    val id: Long? = null,
    val message: String,
    val sender: String,
    val group_name: String,
    val created_at: String? = null
)

class ChatActivity : AppCompatActivity() {

    private lateinit var chatListView: ListView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var groupName: String
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: ChatAdapter
    private lateinit var currentSenderName: String
    private val TAG = "ChatActivity"
    private val supabase = InaSafeApplication.supabaseClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        groupName = intent.getStringExtra("groupName") ?: "Chat"

        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(topAppBar)
        supportActionBar?.title = groupName

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        currentSenderName = user?.displayName?.takeIf { it.isNotBlank() } ?: user?.email ?: "Anónimo"

        chatListView = findViewById(R.id.chatListView)
        messageEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)

        adapter = ChatAdapter(this, mutableListOf(), currentSenderName)
        chatListView.adapter = adapter

        fetchInitialMessages()
        listenForNewMessages()

        sendButton.setOnClickListener {
            sendMessage()
        }
    }

    private fun fetchInitialMessages() {
        lifecycleScope.launch {
            try {
                val today = Calendar.getInstance()
                today.set(Calendar.HOUR_OF_DAY, 0)
                today.set(Calendar.MINUTE, 0)
                today.set(Calendar.SECOND, 0)

                val resultList = supabase.from("messages")
                    .select {
                        filter {
                            eq("group_name", groupName)
                            gte("created_at", today.toInstant().toString())
                        }
                    }
                    .decodeList<JsonObject>()

                val messages = resultList.mapNotNull { record ->
                    val message = record["message"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                    val sender = record["sender"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                    val recordGroupName = record["group_name"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                    val id = record["id"]?.jsonPrimitive?.longOrNull
                    val createdAt = record["created_at"]?.jsonPrimitive?.contentOrNull

                    ChatMessage(
                        id = id,
                        message = message,
                        sender = sender,
                        group_name = recordGroupName,
                        created_at = createdAt
                    )
                }

                adapter.clear()
                adapter.addAll(messages)

                if (adapter.count > 0) {
                    chatListView.setSelection(adapter.count - 1)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching initial messages", e)
                Toast.makeText(
                    this@ChatActivity,
                    "Error al cargar mensajes: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun listenForNewMessages() {
        val channel = supabase.channel("messages")

        lifecycleScope.launch {
            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "messages"
            }.onEach { change ->
                Log.d(TAG, "New change received: $change")
                if (change is PostgresAction.Insert) {

                    val record = change.record as? JsonObject
                    if (record != null) {
                        val message = record["message"]?.jsonPrimitive?.contentOrNull ?: ""
                        val sender = record["sender"]?.jsonPrimitive?.contentOrNull ?: ""
                        val recordGroupName = record["group_name"]?.jsonPrimitive?.contentOrNull ?: ""
                        val id = record["id"]?.jsonPrimitive?.longOrNull
                        val createdAt = record["created_at"]?.jsonPrimitive?.contentOrNull

                        val newMessage = ChatMessage(
                            id = id,
                            message = message,
                            sender = sender,
                            group_name = recordGroupName,
                            created_at = createdAt
                        )

                        if (newMessage.group_name == this@ChatActivity.groupName) {
                            runOnUiThread {
                                adapter.add(newMessage)
                                if (adapter.count > 0) {
                                    chatListView.setSelection(adapter.count - 1)
                                }
                            }
                        }
                    }
                }
            }.launchIn(this)

            channel.subscribe()
        }
    }

    private fun sendMessage() {
        val messageText = messageEditText.text.toString().trim()
        if (messageText.isNotEmpty()) {
            val user = auth.currentUser
            if (user == null) {
                Toast.makeText(this, "No se pudo identificar al usuario. Intente iniciar sesión de nuevo.", Toast.LENGTH_SHORT).show()
                return
            }

            // Ensure we use the same name logic as in onCreate (or update currentSenderName if needed, but it should be static for the session)
            // Ideally we use currentSenderName derived in onCreate, but if user profile updated, maybe fetch again.
            // For now, sticking to the logic in onCreate.
            
            val newMessage = ChatMessage(
                message = messageText,
                sender = currentSenderName,
                group_name = groupName
            )

            lifecycleScope.launch {
                try {
                    supabase.from("messages").insert(newMessage)
                    messageEditText.text.clear()
                } catch (e: Exception) {
                    Log.e(TAG, "Error sending message", e)
                    Toast.makeText(
                        this@ChatActivity,
                        "Error al enviar: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleScope.launch {
            supabase.channel("messages").unsubscribe()
        }
    }
}
