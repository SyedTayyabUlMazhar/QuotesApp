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
import com.magentastudio.quotesapp.Response
import com.magentastudio.quotesapp.UI.Adapter.QuoteAdapter
import com.magentastudio.quotesapp.databinding.FragmentHomeBinding
import kotlinx.coroutines.flow.collect

private const val TAG = "FragmentHome"

class FragmentHome : Fragment()
{
    private lateinit var binding: FragmentHomeBinding

    val viewModel by activityViewModels<QuoteViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View
    {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

        lifecycleScope.launchWhenStarted {
            viewModel.processedQuotes.collect {
                when (it)
                {
                    is Response.Loading -> binding.shimmer.visibility = View.VISIBLE

                    is Response.Default -> Unit
                    is Response.Failure ->
                    {
                        binding.shimmer.visibility = View.INVISIBLE

                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT)
                            .show()
                    }
                    is Response.Success ->
                    {
                        binding.shimmer.visibility = View.INVISIBLE

                        Log.d(TAG, "SEttign adapter")
                        binding.rvQuotes.adapter =
                            QuoteAdapter(requireContext(), viewModel, it.result)
                    }
                }
            }
        }
    }

    fun quoteAdded()
    {
        (binding.rvQuotes.adapter as RecyclerView.Adapter).run { notifyItemInserted(itemCount - 1) }
    }


}