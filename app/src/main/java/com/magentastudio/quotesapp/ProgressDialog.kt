package com.magentastudio.quotesapp

import android.app.AlertDialog
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.widget.ProgressBar

class ProgressDialog : AlertDialog {
    constructor(context: Context?) : super(context) {

        //        SpinKitView spinKitView = new SpinKitView(context);
        //        spinKitView.setIndeterminateDrawable(new WanderingCubes());
        //        setView(spinKitView);
        ProgressBar(context).apply {
            isIndeterminate = true
            indeterminateDrawable.setColorFilter(0xFFFFFFFF.toInt(), PorterDuff.Mode.MULTIPLY)
            setView(this)
        }
        setCancelable(false)
        window!!.setBackgroundDrawable(ColorDrawable(0x00000000))
    }
}

