package com.magentastudio.quotesapp

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.magentastudio.quotesapp.Model.User
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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


        username = FirebaseAuth.getInstance().currentUser!!.displayName!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        et_username.setText(username)
        tv_username_label.setText(username)

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

        btn_save.setOnClickListener { confirmChanges() }

        loadProfilePicture()
    }


    private fun loadProfilePicture()
    {
        MainScope().launch {

            try
            {
                //fetch path form server asynchronously
                val imagePathFromServerDeffered = async { fetchLastUploadedImagePath() }

                // fetch path from cache and if not empty then load image from that path
                val imagePathFromCache = fetchLastUploadedImagePath(true)
                if (!imagePathFromCache.isEmpty())
                {
                    val imageRef = imagePathFromCache.toStorageReference()

                    Glide.with(this@FragmentSetting).load(imageRef).placeholder(R.drawable.avatar)
                        .into(iv_profilePicture)
                }

                // if path from cache is inconsistent from path on server than load image from the later
                val imagePathFromServer = imagePathFromServerDeffered.await()
                if (!imagePathFromServer.contentEquals(imagePathFromCache))
                {
                    val imageRef = imagePathFromServer.toStorageReference()

                    Glide.with(this@FragmentSetting).load(imageRef).placeholder(R.drawable.avatar)
                        .into(iv_profilePicture)
                }

            } catch (e: NullPointerException)
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
            ProgressDialog(context).untilCompletes {
//                val imageUri: Uri? = data?.data
//
//                val newImagePath = uploadImage(imageUri) //upload new image to storage
//                val oldImagePath =
//                    fetchLastUploadedImagePath() // fetch the path of old image from user doc
//
//                updateImagePath(newImagePath)
//
//                deleteImage(oldImagePath)
//
//                val newImageRef = newImagePath.toStorageReference()
//                Glide.with(this).load(newImageRef).into(iv_profilePicture)

                imageUri = data?.data
                Glide.with(this).load(imageUri).into(iv_profilePicture)
            }
    }

    suspend fun uploadImage(imageUri: Uri?): String = withContext(IO)
    {

        val newImageName = UUID.randomUUID().toString() + ".jpeg"
        val imageRef = profileReference.child(newImageName)

        if (imageUri != null)
            imageRef.putFile(imageUri).await()

        imageRef.toString()
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
            path = user!!.profilePic

        } catch (e: Exception)
        {
            Log.i(TAG, "fetchCurrentImagePathLocally() failed")
            e.printStackTrace()
        }

        path
    }

    suspend fun updateImagePath(path: String) = withContext(IO) {
        userDocRef.update("profilePic", path)
    }

    suspend fun deleteImage(path: String) = withContext(IO)
    {
        if (path.isEmpty()) return@withContext
        path.toStorageReference().delete()
    }

    fun String.toStorageReference(): StorageReference
    {
        return Firebase.storage.getReferenceFromUrl(this)
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


    fun confirmChanges()
    {
        ProgressDialog(context).untilCompletes {

            withContext(IO) {
                val currentUser = FirebaseAuth.getInstance().currentUser!!
                val changeRequest =
                    UserProfileChangeRequest.Builder().setDisplayName(username).build()
                currentUser.updateProfile(changeRequest)
            }

            withContext(IO)
            {

                val newImagePath = uploadImage(imageUri) //upload new image to storage
                val oldImagePath =
                    fetchLastUploadedImagePath() // fetch the path of old image from user doc

                updateImagePath(newImagePath)

                deleteImage(oldImagePath)
            }
        }
    }

}