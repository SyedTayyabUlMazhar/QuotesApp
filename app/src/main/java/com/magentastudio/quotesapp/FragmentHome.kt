package com.magentastudio.quotesapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.magentastudio.quotesapp.Model.Quote
import kotlinx.android.synthetic.main.fragment_home.*

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
//        listOf(
//            Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote()
//        ).let {
//            rv_quotes.adapter = QuoteAdapter(context!!, it)
//        }

        val db = Firebase.firestore
        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        val progressDialog = ProgressDialog(context)
        progressDialog.show()

        //get all quotes
        db.collection("quotes").orderBy("votes", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { result ->

                val quotes: List<Quote> = result.toObjects()

                //find if each of the quotes were favorited, upvoted or downvoted by
                //getting the doc IDs of quotes that user has favorited, upvoted, or downvoted.
                //and then matching those doc Ids with IDs of quotes
                db.collection("users").document(userId).get()
                    .addOnSuccessListener { result ->
                        val data = result.data!!

                        val favorites = data["favorites"] as ArrayList<*>
                        val upvoted = data["upvoted"] as ArrayList<*>
                        val downvoted = data["downvoted"] as ArrayList<*>

                        quotes.forEach { quote ->
                            quote.favorited = favorites.contains(quote.docId)
                            quote.upvoted = upvoted.contains(quote.docId)
                            quote.downvoted = downvoted.contains(quote.docId)
                        }
                        rv_quotes.adapter = QuoteAdapter(context!!, quotes)

                        progressDialog.dismiss()

                        quotes.forEachIndexed { index, quote ->
                            Log.i(TAG, "index=$index : quote=$quote")
                        }
                    }
            }
            .addOnFailureListener { exception ->
                Log.i(TAG, "Error getting docs: ${exception.message}")
                exception.printStackTrace()
            }

    }

}