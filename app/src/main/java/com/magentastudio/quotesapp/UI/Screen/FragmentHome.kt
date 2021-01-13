package com.magentastudio.quotesapp.UI.Screen

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.magentastudio.quotesapp.QuoteViewModel
import com.magentastudio.quotesapp.R
import com.magentastudio.quotesapp.Response
import com.magentastudio.quotesapp.UI.Adapter.QuoteAdapter
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.flow.collect

private const val TAG = "FragmentHome"

class FragmentHome : Fragment()
{

    val viewModel by activityViewModels<QuoteViewModel>()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)


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
                                QuoteAdapter(context!!, viewModel, it.result)
                    }
                }
            }
        }
    }

    fun quoteAdded()
    {
        (rv_quotes.adapter as RecyclerView.Adapter).run { notifyItemInserted(itemCount - 1) }
    }


}