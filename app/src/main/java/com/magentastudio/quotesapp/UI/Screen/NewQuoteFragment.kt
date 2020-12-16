package com.magentastudio.quotesapp.UI.Screen

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.magentastudio.quotesapp.QuoteViewModel
import com.magentastudio.quotesapp.R
import kotlinx.android.synthetic.main.activity_new_quote.*

class NewQuoteFragment : DialogFragment()
{

    private val TAG = "NewQuoteFragment"

    private lateinit var viewModel: QuoteViewModel

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
        btn_Confirm.setOnClickListener { dismiss() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)
        if (isAdded)
        {
            val vm by activityViewModels<QuoteViewModel>()
            viewModel = vm
        }
    }


    override fun onCancel(dialog: DialogInterface)
    {
        super.onCancel(dialog)
        Log.i(TAG, "Cancelled")
    }
}