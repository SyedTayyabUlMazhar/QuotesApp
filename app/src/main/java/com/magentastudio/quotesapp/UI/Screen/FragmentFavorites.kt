package com.magentastudio.quotesapp.UI.Screen

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
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
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.android.synthetic.main.fragment_favorites.rv_quotes
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class FragmentFavorites : Fragment()
{

    private val viewModel by viewModels<QuoteViewModel>()

    val TAG = "FragmentFavorites"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_favorites, container, false)


    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

//        shimmer.visibility = View.VISIBLE

        lifecycleScope.launchWhenStarted {
            viewModel.favoriteQuotes.collect {
                when (it)
                {
                    is Response.Default -> Unit
                    is Response.Failure -> Toast.makeText(context, it.message, Toast.LENGTH_SHORT)
                        .show()
                    is Response.Success ->
                    {
//                        shimmer.visibility = View.GONE

                        rv_quotes.adapter =
                            QuoteAdapter1(context!!, viewModel, it.result, showOnlyFavorites = true)
                    }
                }
            }
        }
    }
}

