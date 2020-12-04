package com.magentastudio.quotesapp.UI.Common

import android.content.DialogInterface
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
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle.State.STARTED
import kotlin.random.Random




class ProgressDialog(private var fragManager: FragmentManager) : DialogFragment()
{

    private val TAG = "ProgressDialog"

    private val FILTER = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY)
    private var needsToBeDismissed: Boolean = false

    private var randomId: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        retainInstance = true
        setCancelable(false)

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return ProgressBar(context).apply {
            isIndeterminate = true
            indeterminateDrawable.colorFilter = FILTER
        }
    }

    fun show()
    {
        if (isAdded) return

        randomId = Random(System.nanoTime()).nextInt() % 100

        /* super.show(fragManager, null) was causing java.lang.IllegalStateException: Fragment already added
        * when the loader was in process of getting dismissed and i instantly tapped the button the moment
        * loader went out of visibility, which caused another loaded to be added even though the current one wasnt
        * completely dismissed yet..*/
        Log.d(TAG, "************show()********** id:$randomId")

        super.show(fragManager, null)

        Log.d(TAG, "-----------show()--------- id:$randomId")
        Log.d(TAG, "                              ")

    }

    fun dimiss()
    {
        Log.d(TAG, "************dimiss()********** id:$randomId")

        needsToBeDismissed = true

        if (lifecycle.currentState.isAtLeast(STARTED))
        {
            Log.d(TAG, "Dismissing")
            super.dismiss()
        }
        else
        {
            Log.d(TAG, "Can't dismiss in ${lifecycle.currentState.name} state. Dismissing postponed until onStart()")
        }
        Log.d(TAG, "-----------dismiss()--------- id:$randomId")
        Log.d(TAG, "                              ")


    }

    override fun onStart()
    {
        Log.d(TAG, "************onStart()********** id:$randomId")

        super.onStart()
        Log.d(TAG, "needsToBeDismissed:$needsToBeDismissed")
        if (needsToBeDismissed) //sometimes needsToBeDismissed Is true but the isVisible is false that is why i commented it and now it's working properly.
        {
            Log.d(TAG, "Dismissing")
            super.dismiss()
        }

        Log.d(TAG, "-----------onStart()--------- id:$randomId")
        Log.d(TAG, "                              ")
    }

    override fun onDismiss(dialog: DialogInterface)
    {
        super.onDismiss(dialog)
        needsToBeDismissed = false
    }
}

