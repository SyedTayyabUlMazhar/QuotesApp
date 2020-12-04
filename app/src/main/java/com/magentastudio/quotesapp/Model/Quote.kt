package com.magentastudio.quotesapp.Model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

class Quote
{
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
    // </not part of the doc>

    override fun toString(): String
    {
        return "id:$docId user:$user votes:$votes quote:$quote author:$author favorited:$favorited upvoted:$upvoted downvoted:$downvoted"
    }
}