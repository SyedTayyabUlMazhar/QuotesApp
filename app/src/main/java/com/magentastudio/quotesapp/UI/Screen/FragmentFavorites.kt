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
import com.magentastudio.quotesapp.Response
import com.magentastudio.quotesapp.UI.Adapter.QuoteAdapter
import com.magentastudio.quotesapp.databinding.FragmentFavoritesBinding
import kotlinx.coroutines.flow.collect

class FragmentFavorites : Fragment()
{
    private val TAG = "FragmentFavorites"
    private lateinit var binding: FragmentFavoritesBinding

    private val viewModel by activityViewModels<QuoteViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View
    {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

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

                        binding.rvQuotes.adapter =
                            QuoteAdapter(requireContext(), viewModel, it.result, showOnlyFavorites = true)
                    }
                }
            }
        }
    }
}

