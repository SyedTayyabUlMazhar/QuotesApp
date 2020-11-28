package com.magentastudio.quotesapp.Model


class User
{
    var favorites = ArrayList<String>()
    var upvoted = ArrayList<String>()
    var downvoted = ArrayList<String>()

    var profilePic: String = ""

    fun setUpQuote(quote: Quote)
    {
        quote.let {
            it.favorited = favorites.contains(quote.docId)
            it.upvoted = upvoted.contains(quote.docId)
            it.downvoted = downvoted.contains(quote.docId)
        }
    }
}