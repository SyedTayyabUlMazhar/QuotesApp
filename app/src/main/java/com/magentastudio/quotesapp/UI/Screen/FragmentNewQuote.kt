package com.magentastudio.quotesapp.UI.Screen

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.magentastudio.quotesapp.QuoteViewModel
import com.magentastudio.quotesapp.databinding.FragmentNewQuoteBinding

class FragmentNewQuote : DialogFragment()
{
    private lateinit var binding: FragmentNewQuoteBinding
    private val TAG = "NewQuoteFragment"

    private val viewModel by activityViewModels<QuoteViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View
    {
        binding = FragmentNewQuoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnConfirm.setOnClickListener {
            if (publishQuote())
            {
                (activity as ActivityMain).quoteAdded()
                dismiss()
            }
        }
    }

    override fun onCancel(dialog: DialogInterface)
    {
        super.onCancel(dialog)
        Log.i(TAG, "Cancelled")
    }

    private fun publishQuote(): Boolean
    {
        if (!areQuoteRequirementsSatisfied()) return false

        val quoteText = binding.etQuote.text.toString()
        val author = binding.etAuthor.text.toString()

        viewModel.add(quoteText, author)

        return true
    }

    private fun areQuoteRequirementsSatisfied(): Boolean
    {
        if (binding.etQuote.text.length < 35)
        {
            Snackbar.make(binding.btnCancel, "Quote should be 35-210 characters in length.", Snackbar.LENGTH_LONG).show()
            return false
        }
        else if (binding.etAuthor.text.length < 10)
        {
            Snackbar.make(binding.btnCancel, "Author name should be 10-20 characters in length.", Snackbar.LENGTH_LONG).show()
            return false
        }
        else
            return true
    }
}