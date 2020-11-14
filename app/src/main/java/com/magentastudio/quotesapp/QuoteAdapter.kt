package com.magentastudio.quotesapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.magentastudio.quotesapp.Model.Quote
import kotlinx.android.synthetic.main.quote_box.view.*

class QuoteAdapter : RecyclerView.Adapter<QuoteAdapter.ViewHolder>
{

    var context: Context
    var quotes: List<Quote>
    var allowDeletion: Boolean

    var dummy: Boolean

    val TAG = "QuoteAdapter"

    val db = Firebase.firestore
    val userId = FirebaseAuth.getInstance().currentUser!!.uid
    val userDocRef = db.collection("users").document(userId)

    val colors = arrayOf(R.color.colorAccent, R.color.item_unselected)


    constructor(
        context: Context,
        quotes: List<Quote>,
        allowDeletion: Boolean = false,
        dummy: Boolean = false
    ) : super()
    {

        this.context = context
        this.quotes = quotes
        this.allowDeletion = allowDeletion

        this.dummy = dummy
    }

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
            Glide.with(context).load(quote.user["photo"]).into(iv_userpic)
            tv_username.text = quote.user["name"]

            tv_quote.text = quote.quote
            tv_author.text = quote.author

            tv_vote_count.text = "${quote.votes}"

            favorite_icon.setTint(if (quote.favorited) colors[0] else colors[1])
            upvote_icon.setTint(if (quote.upvoted) colors[0] else colors[1])
            downvote_icon.setTint(if (quote.downvoted) colors[0] else colors[1])


            val listener = View.OnClickListener { buttonClicked ->
                if (!quote.upvoted && !quote.downvoted)
                {
                    when (buttonClicked)
                    {
                        upvote_icon -> upvote(quote, this)
                        else        -> downvote(quote, this)
                    }
                }
                else if (quote.upvoted)
                {
                    undoUpvote(quote, this)
                    if (buttonClicked == downvote_icon)
                        downvote(quote, this)
                }
                else /*if(quote.downvoted)*/
                {
                    undoDownvote(quote, this)
                    if (buttonClicked == upvote_icon)
                        upvote(quote, this)
                }

                notifyItemChanged(position, Unit)

            }

            upvote_icon.setOnClickListener(listener)
            downvote_icon.setOnClickListener(listener)
        }
    }

    override fun getItemCount(): Int = quotes.size


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun ImageView.setTint(@ColorRes color: Int)
    {
        setColorFilter(
            resources.getColor(color), android.graphics.PorterDuff.Mode.SRC_IN
        )
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


    fun upvote(quote: Quote, view: View)
    {
        quote.upvoted = true
        quote.votes++

        userDocRef.update("upvoted", FieldValue.arrayUnion(quote.docId))
        updateVotes(quote.docId, true)
    }

    fun undoUpvote(quote: Quote, view: View)
    {
        quote.upvoted = false
        quote.votes--

        userDocRef.update("upvoted", FieldValue.arrayRemove(quote.docId))
        updateVotes(quote.docId, false)
    }

    fun downvote(quote: Quote, view: View)
    {
        quote.downvoted = true
        quote.votes--

        userDocRef.update("downvoted", FieldValue.arrayUnion(quote.docId))
        updateVotes(quote.docId, false)
    }


    fun undoDownvote(quote: Quote, view: View)
    {
        quote.downvoted = false
        quote.votes++

        userDocRef.update("downvoted", FieldValue.arrayRemove(quote.docId))
        updateVotes(quote.docId, true)
    }

}