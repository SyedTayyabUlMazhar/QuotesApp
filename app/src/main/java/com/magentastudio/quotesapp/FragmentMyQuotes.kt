package com.magentastudio.quotesapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.magentastudio.quotesapp.Model.Quote
import com.magentastudio.quotesapp.Model.User
import kotlinx.android.synthetic.main.fragment_my_quotes.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception

class FragmentMyQuotes : Fragment()
{

    val TAG = "FragmentMyQuotes"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        return inflater.inflate(R.layout.fragment_my_quotes, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
//        mutableListOf(
//            Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote()
//        ).let {
//            rv_quotes.adapter = QuoteAdapter(context!!, it, true, true)
//        }

        MainScope().launch {
            val d = ProgressDialog(context!!)
            d.show()

            val myQuotes = fetchMyQuotes()
            rv_quotes.adapter = QuoteAdapter(context!!, myQuotes, true)

            d.dismiss()
        }

    }

    suspend fun fetchMyQuotes(): MutableList<Quote> = withContext(IO) {

        val db = Firebase.firestore
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        var quotes = listOf<Quote>()

        try
        {
            // non-null asserted because user doc is created after email/password signup or if first time google/fb login.
            val user = db.document("/users/$userId").get().await().toObject<User>()!!

            quotes = db.collection("quotes").whereEqualTo("user.id", userId)
                .orderBy("votes", Query.Direction.DESCENDING).get().await().toObjects()

            quotes.forEach { user.setUpQuote(it) }

        } catch (e: Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "Error getting quotes: ${e.message}")
            showToast("Oops, couldn't get the quotes.")
        }

        quotes.toMutableList()

    }

    suspend fun showToast(message: String) = withContext(Dispatchers.Main)
    {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}