package com.example.banjoshunsuke.line.View

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.banjoshunsuke.line.R
import android.widget.EditText
import android.R.id.edit
import android.content.SharedPreferences
import android.widget.Toast
import android.database.DataSetObserver
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.firebase.client.Firebase
import android.app.ListActivity
import android.view.KeyEvent
import android.view.View
import com.example.banjoshunsuke.line.Model.Message
import com.firebase.client.DataSnapshot
import com.firebase.client.FirebaseError
import com.firebase.client.ValueEventListener
import java.util.*


class MainActivity : ListActivity() {
    private var mUsername: String? = null
    private var mFirebaseRef: Firebase? = null
    private var mConnectedListener: ValueEventListener? = null
    private var mChatListAdapter: MessageListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Make sure we have a mUsername
        setupUsername()

        title = "Chatting as " + mUsername!!

        // Setup our Firebase mFirebaseRef
        mFirebaseRef = Firebase(FIREBASE_URL).child("chat")

        // Setup our input methods. Enter key on the keyboard or pushing the send button
        val inputText = findViewById<View>(R.id.messageInput) as EditText
        inputText.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(textView: TextView, actionId: Int, keyEvent: KeyEvent): Boolean {
                if (actionId == EditorInfo.IME_NULL && keyEvent.action === KeyEvent.ACTION_DOWN) {
                    sendMessage()
                }
                return true
            }
        })

        findViewById<View>(R.id.sendButton).setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                sendMessage()
            }
        })

    }

    public override fun onStart() {
        super.onStart()
        // Setup our view and list adapter. Ensure it scrolls to the bottom as data changes
        val listView = listView
        // Tell our list adapter that we only want 50 messages at a time
        mChatListAdapter = MessageListAdapter(mFirebaseRef!!.limit(50), this, R.layout.chat_laytout, mUsername)
        listView.adapter = mChatListAdapter
        mChatListAdapter!!.registerDataSetObserver(object : DataSetObserver() {
            override fun onChanged() {
                super.onChanged()
                listView.setSelection(mChatListAdapter!!.count - 1)
            }
        })

        // Finally, a little indication of connection status
        mConnectedListener = mFirebaseRef!!.root.child(".info/connected").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val connected = dataSnapshot.getValue() as Boolean
                if (connected) {
                    Toast.makeText(this@MainActivity, "Connected to Firebase", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Disconnected from Firebase", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(firebaseError: FirebaseError) {
                // No-op
            }
        })
    }

    public override fun onStop() {
        super.onStop()
        mFirebaseRef!!.root.child(".info/connected").removeEventListener(mConnectedListener)
        mChatListAdapter!!.cleanup()
    }

    private fun setupUsername() {
        val prefs = application.getSharedPreferences("ChatPrefs", 0)
        mUsername = prefs.getString("username", null)
        if (mUsername == null) {
            val r = Random()
            // Assign a random user name if we don't have one saved.
            mUsername = "JavaUser" + r.nextInt(100000)
            prefs.edit().putString("username", mUsername).commit()
        }
    }

    private fun sendMessage() {
        val inputText = findViewById<View>(R.id.messageInput) as EditText
        val input = inputText.text.toString()
        if (input != "") {
            // Create our 'model', a Chat object
            val chat = mUsername?.let { Message(input, it) }
            // Create a new, auto-generated child of that chat location, and save our chat data there
            mFirebaseRef!!.push().setValue(chat)
            inputText.setText("")
        }
    }

    companion object {

        // TODO: change this to your own Firebase URL
        private val FIREBASE_URL = "https://chatapp-50da3.firebaseio.com/"
    }
}
