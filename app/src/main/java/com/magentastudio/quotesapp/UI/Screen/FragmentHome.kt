package com.magentastudio.quotesapp.UI.Screen

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.magentastudio.quotesapp.Model.Quote
import com.magentastudio.quotesapp.Model.User
import com.magentastudio.quotesapp.R
import com.magentastudio.quotesapp.UI.Adapter.QuoteAdapter
import com.magentastudio.quotesapp.UI.Common.ProgressDialog
import com.magentastudio.quotesapp.UI.Common.showToastMainCoroutine
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.tasks.await
import java.lang.Exception

private const val TAG = "FragmentHome"

class FragmentHome : Fragment()
{

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)


    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

        /* PSEUDO-CODE
        * display progress
        * Get all quotes
        * set their boolean value
        * create adapter
        * set recyclerview adapter
        * dismiss progress dailog*/

        MainScope().launch()
        {
            // getChildFragmentManager will throw exception if this fragment isn't yet attached to the activity.
            if (!isAdded) return@launch

            val _d = ProgressDialog(childFragmentManager)

            _d.show()

            rv_quotes.adapter = QuoteAdapter(context!!, fetchQuotes())

            _d.dimiss()
        }
    }


    /**
     * @return a mutable list of quote, empty if no quotes exist or error.
     */
    suspend fun fetchQuotes(): MutableList<Quote> = withContext(IO)
    {
        val db = Firebase.firestore
//        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        var quotes = mutableListOf<Quote>()

        val quotesQuery = db.collection("quotes").orderBy("votes", Query.Direction.DESCENDING)
        try
        {
            quotes = quotesQuery.get().await().toObjects<Quote>().toMutableList()
            // non-null asserted because user doc is created after on signup
//            val user = db.document("/users/$userId").get().await().toObject<User>()!!
            val user = User.get()

            quotes.forEach { user.setUpQuote(it) }

        }
        catch (e: Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "Error getting quotes: ${e.message}")
            context?.showToastMainCoroutine("Oops, couldn't get the quotes.")
        }

        quotes
    }


}