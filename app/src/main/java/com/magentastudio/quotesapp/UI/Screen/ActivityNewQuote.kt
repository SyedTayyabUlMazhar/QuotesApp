package com.magentastudio.quotesapp.UI.Screen

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.magentastudio.quotesapp.Model.Quote
import com.magentastudio.quotesapp.Model.UserData
import com.magentastudio.quotesapp.R
import com.magentastudio.quotesapp.UI.Common.ProgressDialog
import kotlinx.android.synthetic.main.activity_new_quote.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ActivityNewQuote : AppCompatActivity()
{
    val TAG = "ActivityNewQuote"

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_quote)

        btn_Cancel.setOnClickListener { finish() }
        btn_Confirm.setOnClickListener {
            MainScope().launch()
            {
                val _d = ProgressDialog(supportFragmentManager)
                _d.show()

                val published = publishQuote()
                Log.d(TAG, "Published:$published")

                _d.dismiss()

                if (published) finish()
            }
        }
    }

    suspend fun publishQuote(): Boolean
    {
        if (!areQuoteRequirementsSatisified()) return false

        val quoteText = et_quote.text.toString()
        val author = et_author.text.toString()

        val userMap: MutableMap<String, String> = mutableMapOf()

        var published = false

        withContext(IO)
        {
            val userId = FirebaseAuth.getInstance().currentUser!!.uid

            val user = UserData.get()

            userMap.apply()
            {
                put("id", userId)
                put("name", user.name)
                put("profilePicPath", user.profilePicPath)

            }

            val newQuote = Quote(quoteText, author, userMap, 0)

            try
            {

                Firebase.firestore.collection("quotes").add(newQuote).await()
                published = true
            }
            catch (e: Exception)
            {
                withContext(Main)
                {
                    Snackbar.make(btn_Cancel, "Please check your internet connection", Snackbar.LENGTH_SHORT).show()
                }
                published = false
            }

        }

        return published
    }

    fun areQuoteRequirementsSatisified(): Boolean
    {
        if (et_quote.text.length < 35)
        {
            Snackbar.make(btn_Cancel, "Quote should be 35-210 characters in length.", Snackbar.LENGTH_LONG).show()
            return false
        }
        else if (et_author.text.length < 10)
        {
            Snackbar.make(btn_Cancel, "Author name should be 10-20 characters in length.", Snackbar.LENGTH_LONG).show()
            return false
        }
        else
            return true
    }
}