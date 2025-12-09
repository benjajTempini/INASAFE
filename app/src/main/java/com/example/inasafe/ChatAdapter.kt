package com.example.inasafe

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

class ChatAdapter(context: Context, messages: MutableList<ChatMessage>, private val currentUser: String) : ArrayAdapter<ChatMessage>(context, 0, messages) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val message = getItem(position)

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item_chat, parent, false)
        }

        val messageContainer = view!!.findViewById<LinearLayout>(R.id.messageContainer)
        val senderTextView = view.findViewById<TextView>(R.id.senderTextView)
        val messageTextView = view.findViewById<TextView>(R.id.messageTextView)

        val isMe = message?.sender == currentUser

        senderTextView.text = message?.sender
        messageTextView.text = message?.message

        val params = messageContainer.layoutParams as RelativeLayout.LayoutParams

        if (isMe) {
            params.addRule(RelativeLayout.ALIGN_PARENT_END)
            params.removeRule(RelativeLayout.ALIGN_PARENT_START)
            messageContainer.background = ContextCompat.getDrawable(context, R.drawable.bg_message_sent)
            messageTextView.setTextColor(Color.WHITE)
            senderTextView.visibility = View.GONE
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_START)
            params.removeRule(RelativeLayout.ALIGN_PARENT_END)
            messageContainer.background = ContextCompat.getDrawable(context, R.drawable.bg_message_received)
            messageTextView.setTextColor(Color.BLACK)
            senderTextView.visibility = View.VISIBLE
        }
        
        messageContainer.layoutParams = params

        return view
    }
}