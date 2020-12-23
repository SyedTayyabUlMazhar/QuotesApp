package com.magentastudio.quotesapp

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObjects
import com.magentastudio.quotesapp.Model.Quote
import com.magentastudio.quotesapp.Model.UserData
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class QuoteRepository : UserRepository()
{
    private val TAG = "QuoteRepository"

    private val quotesQuery =
            db.collection(Quote.COLLECTION).orderBy(Quote.VOTES, Query.Direction.DESCENDING)

    suspend fun fetchQuotes() = withContext(IO) {
        val quotes = quotesQuery.get().await().toObjects<Quote>().toMutableList()
        quotes
    }


    /**
     * adds [quote] to favorites' array in db, and returns true if success
     */
    fun favorite(quote: Quote)
    {
        val quoteId = quote.docId
        val shortQuote = quote.quote.take(10) + "..." + quote.quote.takeLast(10)

        userDocRef.update("favorites", FieldValue.arrayUnion(quoteId))
        Log.i(TAG, "favorited quote: $shortQuote ")
    }

    fun unfavorite(quote: Quote)
    {
        val quoteId = quote.docId
        val shortQuote = quote.quote.take(10) + "..." + quote.quote.takeLast(10)

        userDocRef.update("favorites", FieldValue.arrayRemove(quoteId))
        Log.i(TAG, "unfavorited quote:$shortQuote ")
    }



    fun toggleUpvote(quote: Quote)
    {
        var netVoteChange = 0L
        val pendingUserDocUpdates = mutableMapOf<String, Any>()

        netVoteChange += undoDownvoteIfDownvoted(quote, pendingUserDocUpdates)

        if (quote.upvoted)
        {
            quote.upvoted = false
            netVoteChange += -1

            pendingUserDocUpdates.put(
                    UserData.UPVOTED, FieldValue.arrayRemove(quote.docId)
            )

        }
        else
        {
            quote.upvoted = true
            netVoteChange += 1

            pendingUserDocUpdates.put(
                    UserData.UPVOTED, FieldValue.arrayUnion(quote.docId)
            )
        }

        quote.votes += netVoteChange

        val quoteDocRef = quote.docRef()
        quoteDocRef.update(Quote.VOTES, FieldValue.increment(netVoteChange))

        userDocRef.update(pendingUserDocUpdates)
    }

    /**
     * @param quote the quote for which to undo downvote if downvoted
     *
     * @param pendingUserDocUpdates map to which an update is added if the quote is downvoted
     *
     * @return change of vote if any
     */
    fun undoDownvoteIfDownvoted(quote: Quote, pendingUserDocUpdates: MutableMap<String, Any>): Int
    {
        if (quote.downvoted)
        {
            quote.downvoted = false

            pendingUserDocUpdates.put(
                    UserData.DOWNVOTED, FieldValue.arrayRemove(quote.docId)
            )

            return 1
        }

        return 0
    }



    fun toggleDownvote(quote: Quote)
    {
        var netVoteChange = 0L
        val pendingUserDocUpdates = mutableMapOf<String, Any>()

        netVoteChange += undoUpvoteIfUpvoted(quote, pendingUserDocUpdates)

        if (quote.downvoted)
        {
            quote.downvoted = false
            netVoteChange += 1

            pendingUserDocUpdates.put(
                    UserData.DOWNVOTED, FieldValue.arrayRemove(quote.docId)
            )

        }
        else
        {
            quote.downvoted = true
            netVoteChange += -1

            pendingUserDocUpdates.put(
                    UserData.DOWNVOTED, FieldValue.arrayUnion(quote.docId)
            )
        }

        quote.votes += netVoteChange

        val quoteDocRef = quote.docRef()
        quoteDocRef.update(Quote.VOTES, FieldValue.increment(netVoteChange))

        userDocRef.update(pendingUserDocUpdates)
    }

    /**
     * @param quote the quote for which to undo upvote if upvoted
     *
     * @param pendingUserDocUpdates the map to which an update is added, if the quote is upvoted
     *
     * @return change of vote if any
     */
    fun undoUpvoteIfUpvoted(quote: Quote, pendingUserDocUpdates: MutableMap<String, Any>): Int
    {
        if (quote.upvoted)
        {
            quote.upvoted = false

            pendingUserDocUpdates.put(
                    UserData.UPVOTED, FieldValue.arrayRemove(quote.docId)
            )

            return -1
        }

        return 0
    }



    fun add(quote: Quote)
    {
        val docRef = db.collection(Quote.COLLECTION).document()
        quote.docId = docRef.id

        docRef.set(quote)
    }
}