package com.magentastudio.quotesapp.UI.Screen

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.magentastudio.quotesapp.Model.UserData
import com.magentastudio.quotesapp.R
import com.magentastudio.quotesapp.UI.Common.ProgressDialog
import com.magentastudio.quotesapp.UI.Common.toStorageRef
import com.magentastudio.quotesapp.UserRepository
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.activity_sign_up.btnLogin
import kotlinx.android.synthetic.main.activity_sign_up.btnSignUp
import kotlinx.android.synthetic.main.activity_sign_up.etEmail
import kotlinx.android.synthetic.main.activity_sign_up.etPassword
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.util.*


class ActivitySignUp : AppCompatActivity(), OnSuccessListener<AuthResult>,
    OnFailureListener
{
    private val TAG = "ActivitySignUp"
    private val KEY_URI = "URI"

    companion object
    {
        const val SIGN_UP_METHOD = "SIGN_UP_METHOD"
        const val TOKEN = "TOKEN"

        const val SIGN_UP_METHOD_DIRECT = "SIGN_UP_DIRECT" // sign up done with email password
        const val SIGN_UP_METHOD_FB = "SIGN_UP_FB"
        const val SIGN_UP_METHOD_GOOGLE = "SIGN_UP_GOOGLE"
    }

    private lateinit var auth: FirebaseAuth
    private val _d = ProgressDialog.INSTANCE(supportFragmentManager)


    private lateinit var signUpMethod: String //always provided when launching actiivty
    private var token: String? = null

    private lateinit var profileReference: StorageReference
    private val PICK_IMAGE = 124
    private var imageUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()

        signUpMethod = intent.getStringExtra(SIGN_UP_METHOD)!!
        token = intent.getStringExtra(TOKEN)

        profileReference = Firebase.storage.reference.child("/profile")

        if (signUpMethod != SIGN_UP_METHOD_DIRECT)
            hideFieldsRequiredOnlyForDirectSignup()

        imageUri = savedInstanceState?.getParcelable(KEY_URI)
        imageUri?.run { iv_profilePicture.setImageURI(this) }

        setupClickListeners()
    }

    override fun onSaveInstanceState(outState: Bundle)
    {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_URI, imageUri)
    }

    fun setupClickListeners()
    {
        iv_cameraIcon.setOnClickListener { openGallery() }

        btnSignUp.setOnClickListener {
            when (signUpMethod)
            {
                SIGN_UP_METHOD_DIRECT -> signUp()
                SIGN_UP_METHOD_FB -> fbSignUp()
                SIGN_UP_METHOD_GOOGLE -> googleSignUp()
            }
        }

        btnLogin.setOnClickListener {
            finish()
            startActivity(Intent(this, ActivityLogin::class.java))
        }
    }

    fun hideFieldsRequiredOnlyForDirectSignup()
    {
        val gone = View.GONE

        etEmail.visibility = gone
        etPassword.visibility = gone
        etConfirmPassword.visibility = gone
    }


    /**
     * @return true - if all required fields are properly filled.
     */
    fun preSignUpConditionsFullfilled(): Boolean
    {
        val name = et_userName.text.toString()
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        if (signUpMethod == SIGN_UP_METHOD_DIRECT)
        {
            if (email.isEmpty() || password.isEmpty())
            {
                Snackbar.make(
                    btnLogin.rootView,
                    "Please, enter email and password",
                    Snackbar.LENGTH_LONG
                ).show()

                return false
            } else if (!password.contentEquals(confirmPassword))
            {
                Snackbar.make(
                    btnLogin.rootView,
                    "Passwords do not match.",
                    Snackbar.LENGTH_LONG
                ).show()

                return false
            } else if (name.length < 6)
            {
                Snackbar.make(
                    btnLogin.rootView,
                    "Name must be atleast 6 characters long",
                    Snackbar.LENGTH_LONG
                ).show()

                return false
            } else return true
        } else
        {
            if (name.length < 6)
            {
                Snackbar.make(
                    btnLogin.rootView,
                    "Name must be atleast 6 characters long",
                    Snackbar.LENGTH_LONG
                ).show()

                return false
            } else return true
        }
    }

    fun signUp()
    {
        if (!preSignUpConditionsFullfilled()) return

        _d.show()

        val email = etEmail.text.toString()
        val password = etPassword.text.toString()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(this).addOnFailureListener(this)
    }

    fun fbSignUp()
    {
        if (!preSignUpConditionsFullfilled()) return

        val credential = FacebookAuthProvider.getCredential(token!!)
        auth.signInWithCredential(credential)
            .addOnSuccessListener(this).addOnFailureListener(this)
    }

    fun googleSignUp()
    {
        if (!preSignUpConditionsFullfilled()) return

        val credential = GoogleAuthProvider.getCredential(token, null)

        auth.signInWithCredential(credential)
            .addOnSuccessListener(this).addOnFailureListener(this)
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
//        ProgressDialogOld(this).untilCompletes {
//            imageUri = data?.data
//            Glide.with(this).load(imageUri).into(iv_profilePicture)
//        }
    }


    //on signup success
    override fun onSuccess(p0: AuthResult?)
    {
        val user = auth.currentUser!!

        CoroutineScope(IO).launch {
            _d.show()

            uploadProfilePicAndName(user.uid)

            _d.dismiss()
            UserRepository.loggedIn(true)
            navigateToHome()
        }
    }


    //on signup failure
    override fun onFailure(e: Exception)
    {
        _d.dismiss()

        Log.e(TAG, "signup:failure", e)
//        Toast.makeText(this, "Please, check your internet connection", Toast.LENGTH_SHORT).show()
        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }



    private suspend fun uploadProfilePicAndName(userId: String)
    {
        val user = UserData()
        user.name = et_userName.text.toString()

        withContext(Dispatchers.IO)
        {

            user.profilePicPath = uploadImage()

            Firebase.firestore.document("/users/$userId")
                .set(user).await()
        }
    }

    /**
     * Uploads image represented by [imageUri] and returns the path of uploaded image.
     * If [imageUri] is null then an empty string is returned.
     */
    private suspend fun uploadImage(): String = withContext(Dispatchers.IO)
    {

        if (imageUri == null) return@withContext ""

        val newImageName = UUID.randomUUID().toString() + ".jpeg"
        val newImagePath = "profile/$newImageName"

        val imageRef = newImagePath.toStorageRef()

        imageRef.putFile(imageUri!!).await()

        newImagePath
    }


    private fun navigateToHome()
    {
//        Log.i(TAG, "Signed In:  Yes,  email: " + email + " profile: " + photoUrl)
        startActivity(
            Intent(this, ActivityMain::class.java)
        )
    }
}