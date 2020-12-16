package com.magentastudio.quotesapp.Model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class UserData
{
    companion object
    {
        
        const val COLLECTION = "users"

        const val NAME ="name"
        const val PROFILE_PIC_PATH = "profilePicPath"
        const val FAVORITES = "favorites"
        const val UPVOTED = "upvoted"
        const val DOWNVOTED = "downvoted"

        suspend fun get(): UserData = withContext(IO)
        {
            val userId = FirebaseAuth.getInstance().currentUser!!.uid

            Firebase.firestore.document("/users/$userId").get()
                    .await().toObject<UserData>()!!
        }
    }

    var favorites = ArrayList<String>()
    var upvoted = ArrayList<String>()
    var downvoted = ArrayList<String>()

    var name = ""
    var profilePic = ""
    var profilePicPath = ""

    fun setUpQuote(quote: Quote)
    {
        quote.let {
            it.favorited = favorites.contains(quote.docId)
            it.upvoted = upvoted.contains(quote.docId)
            it.downvoted = downvoted.contains(quote.docId)
        }
    }

    fun profilePicReference() = Firebase.storage.getReferenceFromUrl(profilePic)


}