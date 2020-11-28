package com.magentastudio.quotesapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.magentastudio.quotesapp.Model.Quote
import com.magentastudio.quotesapp.Model.User
import com.squareup.okhttp.Dispatcher
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.tasks.await
import java.lang.Exception

private const val TAG = "FragmentHome"

class FragmentHome : Fragment()
{

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
//    {
//        super.onViewCreated(view, savedInstanceState)
////        listOf(
////            Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote()
////        ).let {
////            rv_quotes.adapter = QuoteAdapter(context!!, it)
////        }
//
//        val db = Firebase.firestore
//        val userId = FirebaseAuth.getInstance().currentUser!!.uid
//
//        val progressDialog = ProgressDialog(context)
//        progressDialog.show()
//
//        //get all quotes
//        db.collection("quotes").orderBy("votes", Query.Direction.DESCENDING).get()
//            .addOnSuccessListener { result ->
//
//                val quotes: List<Quote> = result.toObjects()
//
//                //find if each of the quotes were favorited, upvoted or downvoted by
//                //getting the doc IDs of quotes that user has favorited, upvoted, or downvoted.
//                //and then matching those doc Ids with IDs of quotes
//                db.collection("users").document(userId).get()
//                    .addOnSuccessListener { result ->
////                        val data = result.data!!
//
////                        val favorites = data["favorites"] as ArrayList<*>
////                        val upvoted = data["upvoted"] as ArrayList<*>
////                        val downvoted = data["downvoted"] as ArrayList<*>
//                        val user = result.toObject<User>()!!
//
////                        quotes.forEach { quote ->
////                            quote.favorited = favorites.contains(quote.docId)
////                            quote.upvoted = upvoted.contains(quote.docId)
////                            quote.downvoted = downvoted.contains(quote.docId)
////                        }
//                        quotes.forEach { user.setUpQuote(it) }
//                        rv_quotes.adapter = QuoteAdapter(context!!, quotes.toMutableList())
//
//                        progressDialog.dismiss()
//
//                        quotes.forEachIndexed { index, quote ->
//                            Log.i(TAG, "index=$index : quote=$quote")
//                        }
//                    }
//            }
//            .addOnFailureListener { exception ->
//                Log.i(TAG, "Error getting docs: ${exception.message}")
//                exception.printStackTrace()
//            }context?.showToast("Oops, couldn't get the quotes.")
//
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)


        /* PSEUDO-CODE
        * display progress
        * Get all quotes
        * set their boolean value
        * create adapter
        * set recyclerview adapter
        * dismiss progress dailog*/

        CoroutineScope(Main).launch()
        {
            val d = ProgressDialog(context).apply { show() }

            rv_quotes.adapter = QuoteAdapter(context!!, fetchQuotes())

            d.dismiss()
        }
    }


    suspend fun fetchQuotes(): MutableList<Quote> = withContext(IO)
    {
        val db = Firebase.firestore
        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        var quotes = mutableListOf<Quote>()

        val quotesQuery = db.collection("quotes").orderBy("votes", Query.Direction.DESCENDING)
        try
        {
            quotes = quotesQuery.get().await().toObjects<Quote>().toMutableList()
            // non-null asserted because user doc is created after email/password signup or if first time google/fb login.
            val user = db.document("/users/$userId").get().await().toObject<User>()!!

            quotes.forEach {
                user.setUpQuote(it)
            }

        } catch (e: Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "Error getting quotes: ${e.message}")
            context?.showToast("Oops, couldn't get the quotes.")
        }

        quotes
    }


}