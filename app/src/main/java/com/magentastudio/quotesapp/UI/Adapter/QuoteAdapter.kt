package com.magentastudio.quotesapp.UI.Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.magentastudio.quotesapp.Model.Quote
import com.magentastudio.quotesapp.QuoteViewModel
import com.magentastudio.quotesapp.R
import com.magentastudio.quotesapp.UI.Common.loadImage
import com.magentastudio.quotesapp.UI.Common.setTint
import com.magentastudio.quotesapp.databinding.QuoteBoxBinding


class QuoteAdapter(
    var context: Context,
    var viewModel: QuoteViewModel,
    var quotes: MutableList<Quote>,
    var allowDeletion: Boolean = false,
    var dummy: Boolean = false,
    var showOnlyFavorites: Boolean = false
) : RecyclerView.Adapter<QuoteAdapter.ViewHolder>()
{

    private val TAG = "QuoteAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val inflater = LayoutInflater.from(context)
        val binding = QuoteBoxBinding.inflate(inflater, parent, false).apply {
            deleteIcon.visibility = if (allowDeletion) View.VISIBLE else deleteIcon.visibility
        }
        return ViewHolder(binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        if (dummy) return

        val quote = quotes[position]
        holder.bind(quote)
    }

    override fun getItemCount(): Int = quotes.size


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        private val binding: QuoteBoxBinding = QuoteBoxBinding.bind(itemView)

        private lateinit var quote: Quote
        private lateinit var shortQuote: String

        private val COLORS = arrayOf(R.color.colorAccent, R.color.item_unselected)

        fun bind(quote: Quote) = binding.run {
            Log.d(TAG, "bind quote: $quote")

            this@ViewHolder.quote = quote
            this@ViewHolder.shortQuote = quote.quote.take(10) + "..." + quote.quote.takeLast(10)

            val path = quote.user[Quote.USER_PROFILE_PIC_PATH]!!
            if (path.isNotEmpty())
                context.loadImage(path, ivProfilePicture)

            tvUsername.text = quote.user[Quote.USER_NAME]

            tvQuote.text = quote.quote
            tvAuthor.text = quote.author

            tvVoteCount.text = quote.votes.toString()

            favoriteIcon.setTint(if (quote.favorited) COLORS[0] else COLORS[1])
            upvoteIcon.setTint(if (quote.upvoted) COLORS[0] else COLORS[1])
            downvoteIcon.setTint(if (quote.downvoted) COLORS[0] else COLORS[1])

            bindListeners()
        }

        private fun bindListeners() = binding.run {

            favoriteIcon.setOnClickListener {

                Log.i(TAG, "quote:$shortQuote, favorited:${quote.favorited}")

                if (quote.favorited)
                {
                    viewModel.unfavorite(quote)
                    Log.i(TAG, "Unfavorited quote:$shortQuote")

                    if (showOnlyFavorites) notifyItemRemoved()
                    else notifyItemChanged()

                } else
                {
                    viewModel.favorite(quote)
                    Log.i(TAG, "favorited quote:$shortQuote")

                    notifyItemChanged()
                }
            }

            deleteIcon.setOnClickListener {
                viewModel.delete(quote)
                notifyItemRemoved()
            }

            shareIcon.setOnClickListener { share() }

            upvoteIcon.setOnClickListener {
                viewModel.toggleUpvote(quote)
                notifyItemChanged()
            }

            downvoteIcon.setOnClickListener {
                viewModel.toggleDownvote(quote)
                notifyItemChanged()
            }

        }

        private fun share()
        {
            Intent(Intent.ACTION_SEND).run {

                type = "text/plain"

                val shareBody = "${quote.quote}\n\n-${quote.author}"
                val shareSub = "Quote"

                putExtra(Intent.EXTRA_SUBJECT, shareSub)
                putExtra(Intent.EXTRA_TEXT, shareBody)

                startActivity(context, Intent.createChooser(this, "Share using"), null)
            }
        }

        private fun notifyItemRemoved() = notifyItemRemoved(layoutPosition)
        private fun notifyItemChanged() = notifyItemChanged(layoutPosition, Unit)

    }


}