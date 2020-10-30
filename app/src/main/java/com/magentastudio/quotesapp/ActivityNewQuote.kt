package com.magentastudio.quotesapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_new_quote.*

class ActivityNewQuote : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_quote)

        btn_Cancel.setOnClickListener { finish() }
        btn_Confirm.setOnClickListener { finish() }
    }
}