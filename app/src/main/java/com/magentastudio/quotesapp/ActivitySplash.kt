package com.magentastudio.quotesapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ActivitySplash : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val intent = when (null) {
            null -> Intent(this, ActivityLogin::class.java)
            else -> Intent(this, ActivityMain::class.java)
        }

        Handler().postDelayed({
            finish()
            startActivity(intent)
        }, 1000)


    }
}