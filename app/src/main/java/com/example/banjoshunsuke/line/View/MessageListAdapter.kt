package com.example.banjoshunsuke.line.View

import com.example.banjoshunsuke.line.Model.Message
import android.widget.TextView
import android.R.attr.author
import android.app.Activity
import android.graphics.Color
import android.view.View
import com.example.banjoshunsuke.line.R
import com.firebase.client.Query


class MessageListAdapter(ref: Query, activity: Activity, layout: Int, // The mUsername for this client. We use this to indicate which messages originated from this user
                         private val mUsername: String?) : FirebaseListAdapter<Message>(ref, Message::class.java, layout, activity) {

    override fun populateView(view: View, chat: Message) {
        // Map a Chat object to an entry in our listview
        val author = chat.author
        val authorText = view.findViewById(R.id.author) as TextView
        authorText.text = author + ": "
        // If the message was sent by this user, color it differently
        if (author != null && author == mUsername) {
            authorText.setTextColor(Color.RED)
        } else {
            authorText.setTextColor(Color.BLUE)
        }
        (view.findViewById(R.id.message) as TextView).text = chat.content
    }
}