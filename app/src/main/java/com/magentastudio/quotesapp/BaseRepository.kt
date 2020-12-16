package com.magentastudio.quotesapp

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

abstract class BaseRepository
{
    companion object
    {
        @JvmStatic
        protected val db = Firebase.firestore
    }
}

