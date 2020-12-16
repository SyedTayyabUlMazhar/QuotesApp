package com.magentastudio.quotesapp.UI.Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.magentastudio.quotesapp.Model.Quote
import com.magentastudio.quotesapp.QuoteViewModel
import com.magentastudio.quotesapp.R
import com.magentastudio.quotesapp.UI.Common.flip
import com.magentastudio.quotesapp.UI.Common.setTint
import com.magentastudio.quotesapp.UI.Common.showToastMainCoroutine
import kotlinx.android.synthetic.main.quote_box.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import kotlin.math.log


class QuoteAdapter1(
    var context: Context,
    var viewModel: QuoteViewModel,
    var quotes: MutableList<Quote>,
    var allowDeletion: Boolean = false,
    var dummy: Boolean = false,
    var showOnlyFavorites: Boolean = false
) : RecyclerView.Adapter<QuoteAdapter1.ViewHolder>()
{


    val TAG = "QuoteAdapter"

    val db = Firebase.firestore
    val userId = FirebaseAuth.getInstance().currentUser!!.uid
    val userDocRef = db.collection("users").document(userId)

    val colors = arrayOf(R.color.colorAccent, R.color.item_unselected)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.quote_box, parent, false).apply {
            if (allowDeletion) delete_icon.visibility = View.VISIBLE
            return ViewHolder(this)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {

        if (dummy) return

        val quote = quotes[position]

        Log.d(TAG, "quotes.size:${quotes.size}, position:$position")


        holder.itemView.apply {
            val imageRef = Firebase.storage.reference.child(quote.user["profilePicPath"]!!)
            Glide.with(context).load(imageRef).into(iv_profilePicture)
            tv_username.text = quote.user["name"]

            tv_quote.text = quote.quote
            tv_author.text = quote.author

            tv_vote_count.text = "${quote.votes}"

            favorite_icon.setTint(if (quote.favorited) colors[0] else colors[1])
            upvote_icon.setTint(if (quote.upvoted) colors[0] else colors[1])
            downvote_icon.setTint(if (quote.downvoted) colors[0] else colors[1])



            favorite_icon.setOnClickListener {
                val shortQuote = quote.quote.take(10) + "..." + quote.quote.takeLast(10)

                Log.i(TAG, "quote:$shortQuote, favorited:${quote.favorited}")

                if (quote.favorited)
                {
                    viewModel.unfavorite(quote)
                    Log.i(TAG, "Unfavorited quote:$shortQuote")

                    if (showOnlyFavorites) notifyItemRemoved(position)
                    else notifyItemChanged(position)

                } else
                {
                    viewModel.favorite(quote)
                    Log.i(TAG, "favorited quote:$shortQuote")

                    notifyItemChanged(position)
                }
            }

            delete_icon.setOnClickListener {
                CoroutineScope(Main).launch {
                    val deleteSuccess = delete(quote)
                    if (deleteSuccess)
                    {
                        quotes.removeAt(position)
                        notifyItemRemoved(position)
                    }
                }
            }

            share_icon.setOnClickListener { share(quote) }

            upvote_icon.setOnClickListener {
                viewModel.toggleUpvote(quote)
                notifyItemChanged(position)
            }

            downvote_icon.setOnClickListener {
                viewModel.toggleDownvote(quote)
                notifyItemChanged(position)
            }
        }

    }


    override fun getItemCount(): Int = quotes.size


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private fun share(quote: Quote)
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


    /**
     * Deletes quote from firestore
     * @return true if deletion successful false otherwise
     */
    suspend fun delete(quote: Quote): Boolean = withContext(IO) {
        try
        {
            db.document("/quotes/${quote.docId}").delete().await()
            true

        } catch (e: Exception)
        {
            e.printStackTrace()
            Log.e(TAG, "Failed to delete quote:${quote.docId} error: ${e.message}")
            context.showToastMainCoroutine("Couldn't delete the quote.")
            false
        }
    }

}