package com.magentastudio.quotesapp.Model

import com.google.firebase.firestore.DocumentId

class Quote {

    @DocumentId
    lateinit var docId: String

    lateinit var quote: String
    lateinit var author: String

    lateinit var user: Map<String, String>

    var votes: Long = 0

    var favorited = false
    var upvoted = false
    var downvoted = false

    override fun toString(): String {
        return "id:$docId user:$user votes:$votes quote:$quote author:$author favorited:$favorited upvoted:$upvoted downvoted:$downvoted"
    }
}