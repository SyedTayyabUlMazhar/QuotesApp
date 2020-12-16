package com.magentastudio.quotesapp.Model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class Quote
{
    companion object
    {
        const val COLLECTION = "quotes"

        const val QUOTE = "quote"
        const val AUTHOR = "author"
        const val USER_ID = "user.id"
        const val USER_NAME = "user.name"
        const val USER_PROFILE_PIC_PATH = "user.profilePicPath"
        const val VOTES = "votes"

    }

    @DocumentId
    lateinit var docId: String

    lateinit var quote: String
    lateinit var author: String

    lateinit var user: Map<String, String>

    var votes: Long = 0


    // <not part of the doc>
    @get:Exclude
    var favorited = false

    @get:Exclude
    var upvoted = false

    @get:Exclude
    var downvoted = false
    // </not part of the doc>

    constructor()
    {
    }


    constructor(quote: String, author: String, user: Map<String, String>, votes: Long)
    {
        this.quote = quote
        this.author = author
        this.user = user
        this.votes = votes
    }


    fun docRef() = Firebase.firestore.document("$COLLECTION/$docId")

    override fun toString(): String
    {
        return "id:$docId user:$user votes:$votes quote:$quote author:$author favorited:$favorited upvoted:$upvoted downvoted:$downvoted"
    }
}