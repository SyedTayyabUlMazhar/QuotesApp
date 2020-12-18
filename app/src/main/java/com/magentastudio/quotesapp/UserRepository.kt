package com.magentastudio.quotesapp

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.toObject
import com.magentastudio.quotesapp.Model.UserData
import com.magentastudio.quotesapp.UI.Common.toStorageRef
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.util.*

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


    fun changeProfilePic(imageUri: Uri) = flow<Response<Any>> {
        emit(Response.Loading)

        val newImagePath = uploadProfilePic(imageUri)
        updateProfilePicPath(newImagePath)

        val oldImagePath = (userData.value as Response.Success).result.profilePicPath
        deleteProfilePic(oldImagePath)

        Log.d(TAG, "emitting Response.Success")
        emit(Response.Success(Unit))
    }.catch { e ->

        Log.e(TAG, "${e.message}")
        e.printStackTrace()

        Log.d(TAG, "emitting Response.Failure")
        emit(Response.Failure("Failed to change profile picture."))
    }

    /**
     *  Uploads the profile pic represented by [imageUri] and returns the path
     *  of the uploaded image.
     *
     *  @param imageUri the uri of the profile pic to upload.
     *  @return the path of the uploaded pic.
     */
    private suspend fun uploadProfilePic(imageUri: Uri) = withContext(IO)
    {
        val newImageName = UUID.randomUUID().toString() + ".jpeg"
        val newImagePath = "profile/$newImageName"

        val imageRef = newImagePath.toStorageRef()

        imageRef.putFile(imageUri).await()

        newImagePath
    }

    private suspend fun deleteProfilePic(path: String) = withContext(IO)
    {
        val imageRef = path.toStorageRef()

        imageRef.delete().await()
    }


    private fun updateProfilePicPath(path: String)
    {
        userDocRef.update(UserData.PROFILE_PIC_PATH, path)
    }

    fun changeName(newName: String)
    {
        userDocRef.update(UserData.NAME, newName)
    }
}