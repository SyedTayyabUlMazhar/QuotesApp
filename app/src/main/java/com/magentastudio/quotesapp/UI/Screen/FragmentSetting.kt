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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.whenStarted
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.magentastudio.quotesapp.R
import com.magentastudio.quotesapp.Response
import com.magentastudio.quotesapp.UI.Common.ProgressDialog
import com.magentastudio.quotesapp.UI.Common.loadImage
import com.magentastudio.quotesapp.UserRepository
import com.magentastudio.quotesapp.UserViewModel
import kotlinx.android.synthetic.main.fragment_setting.*
import kotlinx.android.synthetic.main.fragment_setting.iv_profilePicture
import kotlinx.android.synthetic.main.quote_box.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate


class FragmentSetting : Fragment()
{
    private val TAG = "FragmentSetting"
    private val PICK_IMAGE: Int = 241

    private val viewModel by viewModels<UserViewModel>()

    private lateinit var profileReference: StorageReference

    private lateinit var userDocRef: DocumentReference

    private var usernameChanged = false
    private var profilePicChanged = false

    private var imageUri: Uri? = null
    private var username: String? = null
    private var email: String? = null

    private val KEY_USERNAME_CHANGED = "KEY_USERNAME_CHANGED"
    private val KEY_PROFILE_PIC_CHANGED = "KEY_PROFILE_PIC_CHANGED"

    private val KEY_IMAGE_URI = "KEY_IMAGE_URI"
    private val KEY_USERNAME = "KEY_USERNAME"
    private val KEY_EMAIL = "KEY_EMAIL"


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)


        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        profileReference = Firebase.storage.reference.child("/profile")
        userDocRef = Firebase.firestore.document("/users/$userId")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_setting, container, false)


    override fun onSaveInstanceState(outState: Bundle)
    {
        super.onSaveInstanceState(outState)
        outState.apply {
            putBoolean(KEY_USERNAME_CHANGED, usernameChanged)
            putBoolean(KEY_PROFILE_PIC_CHANGED, profilePicChanged)

            putParcelable(KEY_IMAGE_URI, imageUri)
            putString(KEY_USERNAME, username)
            putString(KEY_EMAIL, email)
        }
    }

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
            usernameChanged = true
        }

        iv_cancel_icon.setOnClickListener {
            undoLocalChangesToUsername()
            usernameEditable(false)
        }

        btn_save.setOnClickListener { saveChanges() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?)
    {
        super.onActivityCreated(savedInstanceState)

        savedInstanceState?.run {
            usernameChanged = getBoolean(KEY_USERNAME_CHANGED, usernameChanged)
            profilePicChanged = getBoolean(KEY_PROFILE_PIC_CHANGED, profilePicChanged)

            imageUri = getParcelable(KEY_IMAGE_URI)
            username = getString(KEY_USERNAME)
            email = getString(KEY_EMAIL)
        }

        if (savedInstanceState != null)
        {
            loadState()
            if (imageUri == null) loadProfilePic()
        }
        else
            loadProfile()
    }


    private fun loadState()
    {
        imageUri?.let { loadImage(it, iv_profilePicture) }
        if (username != null)
        {
            tv_usernameBig.setText(username)
            et_username.setText(username)
        }
        if (email.isNullOrEmpty()) hideEmailRelatedViews()
        else tv_email.setText(email)
    }


    /**
     *  Loads profile pic, name and email into views.
     *  They are loaded from the realtime user data represented by [UserRepository.userData]
     */
    private fun loadProfile()
    {
        lifecycleScope.launchWhenStarted {

            email = FirebaseAuth.getInstance().currentUser!!.email
            tv_email.setText(email)

            if (email.isNullOrEmpty()) hideEmailRelatedViews()

            UserRepository.userData.collect {
                if (it is Response.Success)
                {
                    val userData = it.result

                    val profilePicPath = userData.profilePicPath
                    username = userData.name

                    tv_usernameBig.setText(username)
                    et_username.setText(username)

                    loadImage(profilePicPath, iv_profilePicture)
                }
            }
        }
    }

    /**
     * loads profile pic from path in [UserRepository.userData]
     * into [iv_profilePicture]
     */
    private fun loadProfilePic()
    {
        lifecycleScope.launchWhenStarted {
            UserRepository.userData.collect {
                if (it is Response.Success)
                {
                    val userData = it.result

                    val profilePicPath = userData.profilePicPath

                    loadImage(profilePicPath, iv_profilePicture)
                }
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
            imageUri?.run {
                loadImage(this, iv_profilePicture)
                profilePicChanged = true
            }
        }
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


    /**
     * To be used when the user email is unknown.( like when fb user hasn't provided it)
     */
    fun hideEmailRelatedViews()
    {
        tv_email_label.visibility = View.GONE
        tv_email.visibility = View.GONE
        divider2.visibility = View.GONE

    }


    fun saveChanges()
    {


//        CoroutineScope(IO).launch {
//            val _d = ProgressDialog.INSTANCE(childFragmentManager)
//            _d.show()
//
//            (1..10).forEach {
//                delay(1000)
//                Log.d(TAG, "Loading progress : ${it * 10}%")
//            }
//            _d.dismiss()
//        }
//        return
        CoroutineScope(IO).launch {
            val _d = ProgressDialog.INSTANCE(childFragmentManager)

            if (profilePicChanged)
            {
                imageUri?.let {
                    viewModel.changeProfilePic(it).conflate().collect { response ->
                        when (response)
                        {
                            is Response.Loading -> _d.show()
                            is Response.Success ->
                            {
                                Log.d(TAG, "Response.Success")

                                _d.dismiss()
                                whenStarted { Toast.makeText(context, "Successfully updated", Toast.LENGTH_SHORT).show() }
                            }
                            is Response.Failure ->
                            {
                                Log.d(TAG, "Response.Failure")

                                _d.dismiss()
                                whenStarted { Toast.makeText(context, response.message, Toast.LENGTH_SHORT).show() }
                            }
                        }
                    }
                }
                profilePicChanged = false
            }

            if (usernameChanged)
            {
                username?.let { viewModel.changeName(it) }
                usernameChanged = false
            }
        }
    }
}