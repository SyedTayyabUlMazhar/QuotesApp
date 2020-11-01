package com.magentastudio.quotesapp

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ConfirmationDialog(context: Context, note: String) {

    lateinit var dialog: AlertDialog
    lateinit var dialogButtonClickListener: DialogButtonClickListener

    init {
        dialog = MaterialAlertDialogBuilder(context).setMessage(note)
            .setPositiveButton("Yes") { dialog, which ->
                dialogButtonClickListener?.yes()
                this.dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, which ->
                dialogButtonClickListener?.no()
                this.dialog.dismiss()
            }
            .create()
    }

    fun show() {
        dialog.show()
    }

    fun dismiss() {
        dialog.dismiss()
    }

    interface DialogButtonClickListener {
        fun yes()
        fun no(){}
    }
}