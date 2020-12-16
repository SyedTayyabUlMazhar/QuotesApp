package com.magentastudio.quotesapp

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.magentastudio.quotesapp.Model.UserData
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await

private const val TAG = "UserRepository"

/**
 * An instance of this class can only be created if a user is signed in.
 */
open class UserRepository : BaseRepository()
{
    companion object
    {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
                ?: throw IllegalStateException("User not signed in")

        val userDocRef = db.document("users/$userId")

        private val _userData = MutableStateFlow<Response<UserData>>(Response.Default(UserData()))
        val userData = _userData.asStateFlow()

        lateinit var job: Job
        lateinit var snapShotListener: ListenerRegistration


        fun loggedIn(loggedIn: Boolean)
        {
            if (!loggedIn)
            {
                if (!job.isActive)
                    return

                Log.i(TAG, "Cancelling job to fetch User Data realtime")
                snapShotListener.remove()
                job.cancel()
            }
            else
            {
                Log.i(TAG, "Starting job to fetch User Data realtime")

                job = GlobalScope.launch(IO)
                {
                    val docRef = db.document("users/$userId")

                    snapShotListener = docRef.addSnapshotListener { docSnapshot, exception ->

                        Log.i(TAG, "Received Sanpshot exception==null: ${exception == null}")

                        val userData = docSnapshot?.toObject<UserData>()
                        //can be null if there is an exception, or there isn't an exception but the document doesn't exists.
                        if (userData == null)
                            _userData.value = Response.Failure("Error fetching user data")
                        else
                            _userData.value = Response.Success(userData)
                    }
                }

//                GlobalScope.launch {
//                    while (true)
//                    {
//                        delay(1000)
//                        Log.i(TAG, "job.isActive: ${job.isActive}")
//                    }
//                }
            }
        }
    }
}