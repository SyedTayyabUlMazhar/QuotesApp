package com.magentastudio.quotesapp.UI.Screen

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.magentastudio.quotesapp.Model.User
import com.magentastudio.quotesapp.R
import com.magentastudio.quotesapp.UI.Common.ProgressDialog
import com.magentastudio.quotesapp.UI.Common.ProgressDialogOld
import com.magentastudio.quotesapp.UI.Common.toStorageReference
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.tasks.await
import java.lang.NullPointerException
import java.util.*


class FragmentSetting : Fragment()
{
    private val TAG = "FragmentSetting"
    private val PICK_IMAGE: Int = 241

    private lateinit var profileReference: StorageReference

    private lateinit var userDocRef: DocumentReference

    private var imageUri: Uri? = null
    private lateinit var username: String


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        profileReference = Firebase.storage.reference.child("/profile")
        userDocRef = Firebase.firestore.document("/users/$userId")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_setting, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)


        iv_cameraIcon.setOnClickListener { openGallery() }
        iv_edit_icon.setOnClickListener {
            usernameEditable(true)
        }

        iv_confirm_icon.setOnClickListener {
            confirmLocalChangesToUserName()
            usernameEditable(false)
        }

        iv_cancel_icon.setOnClickListener {
            undoLocalChangesToUsername()
            usernameEditable(false)
        }

        btn_save.setOnClickListener { saveChanges() }

        loadProfilePictureNameAndEmail()
    }


    private fun loadProfilePictureNameAndEmail()
    {
        MainScope().launch {

            try
            {
                val usernameDeffered = CoroutineScope(IO).async { userDocRef.get().await().toObject<User>()!!.name }
                tv_email.setText(FirebaseAuth.getInstance().currentUser!!.email)

                //fetch path form server asynchronously
                val imagePathFromServerDeffered = async { fetchLastUploadedImagePath() }

                // fetch path from cache and if not empty then load image from that path
                val imagePathFromCache = fetchLastUploadedImagePath(true)
                if (!imagePathFromCache.isEmpty())
                {
                    val imageRef = Firebase.storage.reference.child(imagePathFromCache)

                    Glide.with(this@FragmentSetting).load(imageRef).placeholder(R.drawable.avatar)
                            .into(iv_profilePicture)
                }

                // if path from cache is inconsistent from path on server than load image from the server
                val imagePathFromServer = imagePathFromServerDeffered.await()
                if (!imagePathFromServer.contentEquals(imagePathFromCache))
                {
                    val imageRef = Firebase.storage.reference.child(imagePathFromServer)

                    Glide.with(this@FragmentSetting).load(imageRef).placeholder(R.drawable.avatar)
                            .into(iv_profilePicture)
                }



                username = usernameDeffered.await()

                et_username.setText(username)
                tv_usernameBig.setText(username)
            }
            catch (e: NullPointerException)
            {
                Log.e(TAG, "NPE RAISED")
                e.printStackTrace()
            }
        }
    }

    private fun openGallery()
    {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK)
        {
            imageUri = data?.data
            imageUri?.run { iv_profilePicture.setImageURI(this) }
        }
    }

    suspend fun uploadImage(imageUri: Uri?): String = withContext(IO)
    {

        val newImageName = UUID.randomUUID().toString() + ".jpeg"
        val imageRef = profileReference.child(newImageName)

        if (imageUri != null)
            imageRef.putFile(imageUri).await()

//        imageRef.toString()
        newImageName
    }

    /**
     * Fetches last uploaded image path from server or cache
     * @return last uploaded image path or empty string if(-------there was no image uploaded or fromCache was true and cache was empty-----)
     */
    suspend fun fetchLastUploadedImagePath(fromCache: Boolean = false): String = withContext(IO)
    {
        var path = ""
        try
        {
            val source = if (fromCache) Source.CACHE else Source.DEFAULT

            val user = userDocRef.get(source).await().toObject<User>()
            path = user!!.profilePicPath

        }
        catch (e: Exception)
        {
            Log.i(TAG, "fetchCurrentImagePathLocally() failed")
            e.printStackTrace()
        }

        path
    }

    suspend fun updateImagePath(path: String) = withContext(IO) {
        userDocRef.update("profilePicPath", path)
    }

    suspend fun deleteImage(path: String) = withContext(IO)
    {
        if (path.isEmpty()) return@withContext
//        path.toStorageReference().delete()
        Firebase.storage.reference.child(path).delete()
    }



    fun usernameEditable(state: Boolean)
    {
        iv_edit_icon.visibility = if (state) View.INVISIBLE else View.VISIBLE

        et_username.isEnabled = state
        iv_confirm_icon.visibility = if (state) View.VISIBLE else View.INVISIBLE
        iv_cancel_icon.visibility = if (state) View.VISIBLE else View.INVISIBLE
    }

    fun undoLocalChangesToUsername()
    {
        et_username.setText(username)
    }

    fun confirmLocalChangesToUserName()
    {
        username = et_username.text.toString()
    }

    fun saveChanges()
    {
        MainScope().launch()
        {
            if (!isAdded) return@launch

            val _d = ProgressDialog(childFragmentManager)
            _d.show()

            withContext(IO) { userDocRef.update("name", username) }

            withContext(IO)
            {
                val newImagePath = "profile/" + uploadImage(imageUri) //upload new image to storage
                val oldImagePath = fetchLastUploadedImagePath() // fetch the path of old image from user doc

                updateImagePath(newImagePath)

                deleteImage(oldImagePath)

                _d.dimiss()
            }
        }


    }

}