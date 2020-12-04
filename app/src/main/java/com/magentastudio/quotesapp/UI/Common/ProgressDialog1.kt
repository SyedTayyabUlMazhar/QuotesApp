package com.magentastudio.quotesapp.UI.Common

import android.R.attr
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.State.INITIALIZED
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent




class ProgressDialog1() : DialogFragment(), LifecycleObserver
{

    private val TAG = "ProgressDialog"

    private val filter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
    private var needsToBeDismissed: Boolean = false

    init
    {
        lifecycle.addObserver(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        retainInstance = true
        setCancelable(false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return ProgressBar(context).apply {
            isIndeterminate = true
            indeterminateDrawable.colorFilter = filter
        }
    }

    fun dismissWheneverPossible()
    {
        Log.d(TAG, "dismissWheneverPossible() and state: ${lifecycle.currentState.name}")
        needsToBeDismissed = true

        if (lifecycle.currentState.isAtLeast(INITIALIZED))
            dismiss()
    }

    @OnLifecycleEvent(ON_START)
    private fun dismissIfNeeded()
    {
        Log.d(TAG, "dismissIfNeeded() and needsToBeDismissed:${needsToBeDismissed} isVisible:${needsToBeDismissed}")
        if (needsToBeDismissed && isVisible)
            dismiss()
    }


}

