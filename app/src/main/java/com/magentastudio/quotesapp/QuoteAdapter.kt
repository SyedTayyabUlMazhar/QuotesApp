package com.magentastudio.quotesapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.magentastudio.quotesapp.Model.Quote
import kotlinx.android.synthetic.main.quote_box.view.*

class QuoteAdapter : RecyclerView.Adapter<QuoteAdapter.ViewHolder> {

    var context: Context
    var quotes: List<Quote>
    var allowDeletion: Boolean

    constructor(context: Context, quotes: List<Quote>, allowDeletion: Boolean = false) : super() {

        this.context = context
        this.quotes = quotes
        this.allowDeletion = allowDeletion
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.quote_box, parent, false).apply {
            if (allowDeletion) delete_icon.visibility = View.VISIBLE
            return ViewHolder(this)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    }

    override fun getItemCount(): Int {
        return quotes.size
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}