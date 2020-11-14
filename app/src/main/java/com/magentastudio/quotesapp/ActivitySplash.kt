package com.magentastudio.quotesapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_sign_up.*

class ActivitySplash : AppCompatActivity()
{
    val TAG = "ActivitySplash"

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val intent = when (FirebaseAuth.getInstance().currentUser)
        {
            null -> Intent(this, ActivityLogin::class.java)
            else -> Intent(this, ActivityMain::class.java)
        }

        Handler().postDelayed({
            finish()
            startActivity(intent)
        }, 1000)


        val currentUser = FirebaseAuth.getInstance().currentUser
        Log.i(
            TAG,
            ("Signed In: " + if (currentUser != null) "Yes" else "No") + " email: " + currentUser?.email + " name: " + currentUser?.displayName
        )
    }
}