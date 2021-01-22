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
import com.magentastudio.quotesapp.Response
import com.magentastudio.quotesapp.UI.Common.ProgressDialog
import com.magentastudio.quotesapp.UI.Common.loadImage
import com.magentastudio.quotesapp.UserRepository
import com.magentastudio.quotesapp.UserViewModel
import com.magentastudio.quotesapp.databinding.FragmentSettingBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch


class FragmentSetting : Fragment()
{
    private val TAG = "FragmentSetting"
    private lateinit var binding: FragmentSettingBinding

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View
    {
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = binding.run {
        super.onViewCreated(view, savedInstanceState)

        ivCameraIcon.setOnClickListener { openGallery() }
        ivEditIcon.setOnClickListener {
            usernameEditable(true)
        }

        ivConfirmIcon.setOnClickListener {
            confirmLocalChangesToUserName()
            usernameEditable(false)
            usernameChanged = true
        }

        ivCancelIcon.setOnClickListener {
            undoLocalChangesToUsername()
            usernameEditable(false)
        }

        btnSave.setOnClickListener { saveChanges() }
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
        } else
            loadProfile()
    }


    private fun loadState() = binding.run {

        imageUri?.let { loadImage(it, ivProfilePicture) }
        if (username != null)
        {
            tvUsernameBig.setText(username)
            etUsername.setText(username)
        }
        if (email.isNullOrEmpty()) hideEmailRelatedViews()
        else tvEmail.setText(email)

    }


    /**
     *  Loads profile pic, name and email into views.
     *  They are loaded from the realtime user data represented by [UserRepository.userData]
     */
    private fun loadProfile() = lifecycleScope.launchWhenStarted {

        email = FirebaseAuth.getInstance().currentUser!!.email
        binding.tvEmail.setText(email)

        if (email.isNullOrEmpty()) hideEmailRelatedViews()

        UserRepository.userData.collect {
            if (it is Response.Success)
            {
                val userData = it.result

                val profilePicPath = userData.profilePicPath
                username = userData.name

                binding.tvUsernameBig.setText(username)
                binding.etUsername.setText(username)

                loadImage(profilePicPath, binding.ivProfilePicture)
            }
        }

    }

    /**
     * loads profile pic from path in [UserRepository.userData]
     * into [binding.ivProfilePicture]
     */
    @Suppress("KDocUnresolvedReference")
    private fun loadProfilePic()
    {
        lifecycleScope.launchWhenStarted {
            UserRepository.userData.collect {
                if (it is Response.Success)
                {
                    val userData = it.result

                    val profilePicPath = userData.profilePicPath

                    loadImage(profilePicPath, binding.ivProfilePicture)
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
                loadImage(this, binding.ivProfilePicture)
                profilePicChanged = true
            }
        }
    }



    private fun usernameEditable(state: Boolean) = binding.run {

        ivEditIcon.visibility = if (state) View.INVISIBLE else View.VISIBLE

        etUsername.isEnabled = state
        ivConfirmIcon.visibility = if (state) View.VISIBLE else View.INVISIBLE
        ivCancelIcon.visibility = if (state) View.VISIBLE else View.INVISIBLE

    }

    private fun undoLocalChangesToUsername()
    {
        binding.etUsername.setText(username)
    }

    private fun confirmLocalChangesToUserName()
    {
        username = binding.etUsername.text.toString()
    }


    /**
     * To be used when the user email is unknown.( like when fb user hasn't provided it)
     */
    private fun hideEmailRelatedViews() = binding.run {
        tvEmailLabel.visibility = View.GONE
        tvEmail.visibility = View.GONE
        divider2.visibility = View.GONE
    }


    private fun saveChanges() = CoroutineScope(IO).launch {
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
                            whenStarted {
                                Toast.makeText(
                                    context,
                                    "Successfully updated",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        is Response.Failure ->
                        {
                            Log.d(TAG, "Response.Failure")

                            _d.dismiss()
                            whenStarted {
                                Toast.makeText(
                                    context,
                                    response.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
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