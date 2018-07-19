package com.example.banjoshunsuke.line.View

import android.app.Application
import com.firebase.client.Firebase


class MessageApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Firebase.setAndroidContext(this)
    }
}