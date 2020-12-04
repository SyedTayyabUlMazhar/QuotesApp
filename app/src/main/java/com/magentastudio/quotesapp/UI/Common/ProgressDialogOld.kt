package com.magentastudio.quotesapp.UI.Common

import android.app.AlertDialog
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.widget.ProgressBar
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.lang.Exception

class ProgressDialogOld : AlertDialog
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
    fun untilCompletes(block: suspend () -> Unit): Job = MainScope().launch {
        show()

        block()
//            View=com.android.internal.policy.impl.PhoneWindow$DecorView{25dc0e16 V.E..... R......D 0,0-636,72} not attached to window manager
        //            android.view.WindowLeaked: Activity com.magentastudio.quotesapp.UI.Screen.ActivitySignUp has leaked
        try
        {
            dismiss()
        } catch (e: Exception)
        {
        }

    }

}

