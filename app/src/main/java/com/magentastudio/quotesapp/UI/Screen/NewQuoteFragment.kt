package com.magentastudio.quotesapp.UI.Screen

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import com.magentastudio.quotesapp.QuoteViewModel
import com.magentastudio.quotesapp.R
import kotlinx.android.synthetic.main.activity_new_quote.*

class NewQuoteFragment : DialogFragment()
{
    private val TAG = "NewQuoteFragment"

    private val viewModel by activityViewModels<QuoteViewModel>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View
    {
        return inflater.inflate(R.layout.activity_new_quote, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        btn_Cancel.setOnClickListener { dismiss() }
        btn_Confirm.setOnClickListener {
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

    fun publishQuote(): Boolean
    {
        if (!areQuoteRequirementsSatisified()) return false

        val quoteText = et_quote.text.toString()
        val author = et_author.text.toString()

        viewModel.add(quoteText, author)

        return true
    }

    fun areQuoteRequirementsSatisified(): Boolean
    {
        if (et_quote.text.length < 35)
        {
            Snackbar.make(btn_Cancel, "Quote should be 35-210 characters in length.", Snackbar.LENGTH_LONG).show()
            return false
        }
        else if (et_author.text.length < 10)
        {
            Snackbar.make(btn_Cancel, "Author name should be 10-20 characters in length.", Snackbar.LENGTH_LONG).show()
            return false
        }
        else
            return true
    }
}