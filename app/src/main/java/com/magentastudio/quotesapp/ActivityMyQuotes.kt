package com.magentastudio.quotesapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.magentastudio.quotesapp.Model.Quote
import kotlinx.android.synthetic.main.activity_home.*

class ActivityMyQuotes : ActivityBase() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_quotes)

        setTitle("My Quotes")
        setSelectedNavigationItem(R.id.myQuotes)

        listOf(
            Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote()
        ).let {
            rv_quotes.adapter = QuoteAdapter(this, it, true)
        }
    }
}