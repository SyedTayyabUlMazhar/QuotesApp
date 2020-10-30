package com.magentastudio.quotesapp.Model

class Quote {
    lateinit var profilePicUrl:String
    lateinit var username:String

    lateinit var text:String
    lateinit var author:String

    var upvotes:Long = 0
    var downvotes:Long = 0
}