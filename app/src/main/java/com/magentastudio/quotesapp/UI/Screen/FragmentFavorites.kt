package com.magentastudio.quotesapp.UI.Screen

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.magentastudio.quotesapp.Model.Quote
import com.magentastudio.quotesapp.Model.User
import com.magentastudio.quotesapp.R
import com.magentastudio.quotesapp.UI.Common.ProgressDialogOld
import com.magentastudio.quotesapp.UI.Adapter.QuoteAdapter
import com.magentastudio.quotesapp.UI.Common.ProgressDialog
import com.magentastudio.quotesapp.UI.Common.showToastMainCoroutine
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class FragmentFavorites : Fragment()
{

    val TAG = "FragmentFavorites"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_favorites, container, false)



    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
//         listOf(
//            Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote()
//        ).let {
//            rv_quotes.adapter = QuoteAdapter(context!!, it, dummy = true)
//        }

        MainScope().launch {

            if (!isAdded) return@launch

            val d = ProgressDialog(childFragmentManager)
            d.show()

            val favorites = fetchFavoriteQuotes()
            rv_quotes.adapter = QuoteAdapter(context!!, favorites, showOnlyFavorites = true)

            d.dismiss()
        }
    }

    suspend fun fetchFavoriteQuotes(): MutableList<Quote> = withContext(IO) {
        val db = Firebase.firestore
//        val userId = FirebaseAuth.getInstance().currentUser!!.uid


        val favoriteQuotes = mutableListOf<Deferred<Quote>>()

        try
        {
//            val userDocRef = db.document("/users/$userId")
            // non-null asserted because user doc is created after email/password signup or if first time google/fb login.
//            val user = userDocRef.get().await().toObject<User>()!!
            val user = User.get()

            user.favorites.forEachIndexed()
            { index, id ->
                //adding quotes as deferred because if you add favoriteQutoes inside the async then it can collide with other asyncs and values can get overwritten
                favoriteQuotes += async()
                {
                    val quote = db.document("/quotes/$id").get().await().toObject<Quote>()!!
                    user.setUpQuote(quote)

                    quote
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "Error getting quotes: ${e.message}")
            context?.showToastMainCoroutine("Oops, couldn't get the quotes.")
        }

        Log.i(
                TAG,
                "--------------RETURNING---------------- favoriteQuotes.size: ${favoriteQuotes.size}"
        )

        favoriteQuotes.awaitAll().toMutableList()
    }
}

