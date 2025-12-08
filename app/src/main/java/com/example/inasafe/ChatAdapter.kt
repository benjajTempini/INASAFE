package com.example.inasafe

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

// This adapter is now designed to work with the new ChatMessage data class
class ChatAdapter(context: Context, messages: MutableList<ChatMessage>) : ArrayAdapter<ChatMessage>(context, 0, messages) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val message = getItem(position)

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_chat, parent, false)
        }

        val senderTextView = view!!.findViewById<TextView>(R.id.senderTextView)
        val messageTextView = view.findViewById<TextView>(R.id.messageTextView)

        // The ChatMessage object now directly provides sender and message fields
        senderTextView.text = message?.sender
        messageTextView.text = message?.message

        return view
    }
}