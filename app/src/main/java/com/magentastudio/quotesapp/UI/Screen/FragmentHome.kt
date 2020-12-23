package com.magentastudio.quotesapp.UI.Screen

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.magentastudio.quotesapp.Model.Quote
import com.magentastudio.quotesapp.Model.UserData
import com.magentastudio.quotesapp.QuoteViewModel
import com.magentastudio.quotesapp.R
import com.magentastudio.quotesapp.Response
import com.magentastudio.quotesapp.UI.Adapter.QuoteAdapter
import com.magentastudio.quotesapp.UI.Adapter.QuoteAdapter1
import com.magentastudio.quotesapp.UI.Common.ProgressDialog
import com.magentastudio.quotesapp.UI.Common.showToastMainCoroutine
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.tasks.await
import java.lang.Exception

private const val TAG = "FragmentHome"

class FragmentHome : Fragment()
{

    val viewModel by activityViewModels<QuoteViewModel>()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

//    override fun onActivityCreated(savedInstanceState: Bundle?)
//    {
//        super.onActivityCreated(savedInstanceState)
//
//        /* PSEUDO-CODE
//        * display progress
//        * Get all quotes
//        * set their boolean value
//        * create adapter
//        * set recyclerview adapter
//        * dismiss progress dailog*/
//
//        MainScope().launch()
//        {
//            // getChildFragmentManager will throw exception if this fragment isn't yet attached to the activity.
//            if (!isAdded) return@launch
//
//            val _d = ProgressDialog(childFragmentManager)
//
//            _d.show()
//
//            rv_quotes.adapter = QuoteAdapter(context!!, fetchQuotes())
//
//            _d.dimiss()
//        }
//    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)


        lifecycleScope.launchWhenStarted {
            viewModel.processedQuotes.collect {
                when (it)
                {
                    is Response.Loading -> shimmer.visibility = View.VISIBLE

                    is Response.Default -> Unit
                    is Response.Failure ->
                    {
                        shimmer.visibility = View.INVISIBLE

                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT)
                                .show()
                    }
                    is Response.Success ->
                    {
                        shimmer.visibility = View.INVISIBLE

                        Log.d(TAG, "SEttign adapter")
                        rv_quotes.adapter =
                                QuoteAdapter1(context!!, viewModel, it.result)
                    }
                }
            }
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
            val user = UserData.get()
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

    fun quoteAdded()
    {
        (rv_quotes.adapter as RecyclerView.Adapter).run { notifyItemInserted(itemCount - 1) }
//        rv_quotes.adapter?.run { notifyItemInserted(itemCount - 1) }
    }


}