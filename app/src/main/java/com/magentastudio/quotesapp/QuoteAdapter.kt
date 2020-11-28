package com.magentastudio.quotesapp

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.magentastudio.quotesapp.Model.Quote
import kotlinx.android.synthetic.main.quote_box.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception


class QuoteAdapter(
    var context: Context,
    var quotes: MutableList<Quote>,
    var allowDeletion: Boolean = false,
    var dummy: Boolean = false,
    var showOnlyFavorites: Boolean = false
) : RecyclerView.Adapter<QuoteAdapter.ViewHolder>()
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
//        holder.itemView.favorite_icon.setOnClickListener {
//            it as ImageView
//
//            quotes[position].favorited = !quotes[position].favorited
//
//            if (quotes[position].favorited) {
//                it.setTint(R.color.colorAccent)
//            } else {
//                it.setTint(R.color.item_unselected)
//            }
//        }
        if (dummy) return

        val quote = quotes[position]


        holder.itemView.apply {
            Glide.with(context).load(quote.user["photo"]).into(iv_profilePicture)
            tv_username.text = quote.user["name"]

            tv_quote.text = quote.quote
            tv_author.text = quote.author

            tv_vote_count.text = "${quote.votes}"

            favorite_icon.setTint(if (quote.favorited) colors[0] else colors[1])
            upvote_icon.setTint(if (quote.upvoted) colors[0] else colors[1])
            downvote_icon.setTint(if (quote.downvoted) colors[0] else colors[1])



            favorite_icon.setOnClickListener {
                quote::favorited.flip()
                favorite(quote)
                if (showOnlyFavorites && !quote.favorited)
                {
                    quotes.removeAt(position)
                    notifyItemRemoved(position)
                }
                else
                    notifyItemChanged(position, Unit)
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

            val listener = View.OnClickListener { buttonClicked ->
                if (!quote.upvoted && !quote.downvoted)
                {
                    when (buttonClicked)
                    {
                        upvote_icon -> upvote(quote)
                        else        -> downvote(quote)
                    }
                }
                else if (quote.upvoted)
                {
                    undoUpvote(quote)
                    if (buttonClicked == downvote_icon)
                        downvote(quote)
                }
                else /*if(quote.downvoted)*/
                {
                    undoDownvote(quote)
                    if (buttonClicked == upvote_icon)
                        upvote(quote)
                }

                notifyItemChanged(position, Unit)

            }

            upvote_icon.setOnClickListener(listener)
            downvote_icon.setOnClickListener(listener)
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
     * increments or decrements the vote count of quote by 1.
     * @param increase whether to increment or decrement the vote count
     */
    fun updateVotes(quoteId: String, increase: Boolean)
    {
        val value = if (increase) 1L else -1L

        db.collection("quotes").document(quoteId).update("votes", FieldValue.increment(value))
    }


    fun upvote(quote: Quote)
    {
        quote.upvoted = true
        quote.votes++


        userDocRef.update("upvoted", FieldValue.arrayUnion(quote.docId))
        updateVotes(quote.docId, true)
    }

    fun undoUpvote(quote: Quote)
    {
        quote.upvoted = false
        quote.votes--

        userDocRef.update("upvoted", FieldValue.arrayRemove(quote.docId))
        updateVotes(quote.docId, false)
    }

    fun downvote(quote: Quote)
    {
        quote.downvoted = true
        quote.votes--

        userDocRef.update("downvoted", FieldValue.arrayUnion(quote.docId))
        updateVotes(quote.docId, false)
    }


    fun undoDownvote(quote: Quote)
    {
        quote.downvoted = false
        quote.votes++

        userDocRef.update("downvoted", FieldValue.arrayRemove(quote.docId))
        updateVotes(quote.docId, true)
    }


    /**
     * adds quote to the array of favorite quotes in db if quote.favorited==true else removes it.
     */
    fun favorite(quote: Quote)
    {
        quote.apply {
            if (favorited)
                userDocRef.update("favorites", FieldValue.arrayUnion(docId))
            else
            {
                userDocRef.update("favorites", FieldValue.arrayRemove(docId))
            }
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
            context.showToast("Couldn't delete the quote.")
            false
        }
    }

}