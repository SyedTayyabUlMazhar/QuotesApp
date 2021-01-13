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
import kotlinx.android.synthetic.main.fragment_my_quotes.*
import kotlinx.coroutines.flow.collect

class FragmentMyQuotes : Fragment()
{

    private val viewModel by activityViewModels<QuoteViewModel>()

    val TAG = "FragmentMyQuotes"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_my_quotes, container, false)


    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
//        mutableListOf(
//            Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote()
//        ).let {
//            rv_quotes.adapter = QuoteAdapter(context!!, it, true, true)
//        }

        lifecycleScope.launchWhenStarted {
            viewModel.myQuotes.collect {
                when (it)
                {
                    is Response.Default -> Unit
                    is Response.Failure -> Toast.makeText(context, it.message, Toast.LENGTH_SHORT)
                            .show()
                    is Response.Success ->
                    {
                        rv_quotes.adapter =
                                QuoteAdapter(context!!, viewModel, it.result, allowDeletion = true)
                    }
                }
            }
        }

    }
}