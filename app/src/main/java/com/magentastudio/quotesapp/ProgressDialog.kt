package com.magentastudio.quotesapp

import android.app.AlertDialog
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.widget.ProgressBar
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.annotation.Nullable

class ProgressDialog : AlertDialog
{
    constructor(context: Context?) : super(context)
    {

        val progressBar = ProgressBar(context).apply {
            isIndeterminate = true
            indeterminateDrawable.setColorFilter(0xFFFFFFFF.toInt(), PorterDuff.Mode.MULTIPLY)
        }
        setView(progressBar)

        setCancelable(false)
        window!!.setBackgroundDrawable(ColorDrawable(0x00000000))
    }


    /**
     * Shows a progress dialog until the suspend block is completed.
     * @param block A suspend block that runs in MainScope.
     */
    fun untilCompletes(block: suspend () -> Unit)
    {
        MainScope().launch {
            show()

            block()

            dismiss()
        }
    }


}

