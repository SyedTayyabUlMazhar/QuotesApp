package com.magentastudio.quotesapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.magentastudio.quotesapp.Model.Quote
import kotlinx.android.synthetic.main.activity_home.*

class ActivityFavorites : ActivityBase() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        setTitle("Favorites")
        setSelectedNavigationItem(R.id.favorites)

        listOf(
            Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote()
        ).let {
            rv_quotes.adapter = QuoteAdapter(this, it)
        }


    }
}