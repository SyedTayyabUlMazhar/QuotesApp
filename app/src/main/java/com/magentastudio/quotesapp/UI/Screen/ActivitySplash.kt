package com.magentastudio.quotesapp.UI.Screen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.magentastudio.quotesapp.R
import com.magentastudio.quotesapp.UserRepository

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
            else ->
            {
                UserRepository.loggedIn(true)
                Intent(this, ActivityMain::class.java)
            }
        }

        Handler().postDelayed({
            finish()
            startActivity(intent)
        }, 1000)


        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser.run {
            Log.i(TAG, "Signed In: ${this != null}, email: ${this?.email}, id:${this?.uid}")
        }
    }
}