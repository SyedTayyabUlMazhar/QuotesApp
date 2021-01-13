package com.magentastudio.quotesapp.UI.Screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.magentastudio.quotesapp.QuoteViewModel
import com.magentastudio.quotesapp.R
import com.magentastudio.quotesapp.Response
import com.magentastudio.quotesapp.UI.Adapter.QuoteAdapter
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.coroutines.flow.collect

class FragmentFavorites : Fragment()
{

    private val viewModel by activityViewModels<QuoteViewModel>()

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
                            QuoteAdapter(context!!, viewModel, it.result, showOnlyFavorites = true)
                    }
                }
            }
        }
    }
}

