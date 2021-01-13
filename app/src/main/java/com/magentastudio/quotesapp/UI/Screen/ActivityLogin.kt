package com.magentastudio.quotesapp.UI.Screen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.magentastudio.quotesapp.UI.Screen.ActivitySignUp.Companion.SIGN_UP_METHOD_DIRECT
import com.magentastudio.quotesapp.UI.Screen.ActivitySignUp.Companion.SIGN_UP_METHOD_FB
import com.magentastudio.quotesapp.UI.Screen.ActivitySignUp.Companion.SIGN_UP_METHOD_GOOGLE
import com.magentastudio.quotesapp.UI.Screen.ActivitySignUp.Companion.SIGN_UP_METHOD
import com.magentastudio.quotesapp.UI.Screen.ActivitySignUp.Companion.TOKEN
import com.magentastudio.quotesapp.R
import com.magentastudio.quotesapp.UI.Common.ProgressDialog
import com.magentastudio.quotesapp.UserRepository
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext



class ActivityLogin : AppCompatActivity()
{
    private val TAG = "ActivityLogin"
    private val SIGN_IN_FAILED_MESSAGE =
        "Authentication Failed. Ensure that you've entered correct email, password and your internet is working properly"

    private lateinit var auth: FirebaseAuth

    private val GOOGLE_SIGN_IN = 2412
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private lateinit var callbackManager: CallbackManager
    private val permissions = listOf("email", "public_profile")


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        mGoogleSignInClient = googleSignInClient()

        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance()
            .registerCallback(callbackManager, facebookLoginCompleteCallback())

        initiateButtonClickListeners()
    }

    private fun initiateButtonClickListeners()
    {
        btnLogin.setOnClickListener { signin() }
        btnLoginGoogle.setOnClickListener { startGoogleSigninFlow() }
        btnLoginFacebook.setOnClickListener { startFbSigninFlow() }
        btnSignUp.setOnClickListener { startSignUpActivity(SIGN_UP_METHOD_DIRECT, null) }
    }

    fun navigateToHome() = startActivity(
        Intent(this@ActivityLogin, ActivityMain::class.java)
    )


    /**
     * Signin to firebase with email/password
     */
    fun signin()
    {
        hideKeyboard(this)

        val email = etEmail.text.toString()
        val password = etPassword.text.toString()

        //some validation
        if (email.isEmpty() || password.isEmpty())
        {
            Snackbar.make(btnLogin, "Please, enter email and password", Snackbar.LENGTH_LONG).show()
            return
        }

        CoroutineScope(IO).launch {
            val _d = ProgressDialog.INSTANCE(supportFragmentManager)

            _d.show()
            try
            {
                auth.signInWithEmailAndPassword(email, password).await()
                Log.d(TAG, "Signin Task Successful")
                loginSuccess()
            } catch (e: Exception)
            {
                Snackbar.make(btnLogin, "${e.message}", Snackbar.LENGTH_LONG).show()
                Log.e(TAG, "signIn:failed $e")
            }
            _d.dismiss()
        }
    }



    fun googleSignInClient(): GoogleSignInClient
    {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(this, gso)
    }

    fun startGoogleSigninFlow()
    {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(token: String)
    {
        val credential = GoogleAuthProvider.getCredential(token, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful)
                {
                    mGoogleSignInClient.signOut()

                    val user = auth.currentUser!!

                    MainScope().launch {
                        if (doesUserDocExist(user.uid))
                            loginSuccess()
                        else
                        {
                            FirebaseAuth.getInstance().signOut()
                            startSignUpActivity(SIGN_UP_METHOD_GOOGLE, token)
                        }
                    }
                } else
                {
                    mGoogleSignInClient.signOut()
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Snackbar.make(btnLogin, "Failed: " + task.exception?.message, Snackbar.LENGTH_LONG)
                        .show()
                }
            }
    }



    fun facebookLoginCompleteCallback() = object : FacebookCallback<LoginResult>
    {
        override fun onSuccess(loginResult: LoginResult)
        {
            Log.d(TAG, "*********** LoginManager callback onSuccess *************")
            Log.d(TAG, "facebook:onSuccess:$loginResult")
            firebaseAuthWithFb(loginResult.accessToken.token)
        }

        override fun onCancel()
        {
            Log.d(TAG, "facebook:onCancel")
        }

        override fun onError(exception: FacebookException)
        {
            Log.d(TAG, "facebook:onError", exception)

        }
    }

    fun startFbSigninFlow()
    {
        Log.d(TAG, "*********** signInWithFb *************")
        LoginManager.getInstance().logIn(this, permissions)
    }

    private fun firebaseAuthWithFb(token: String)
    {
        Log.d(TAG, "*********** handleFacebookAccessToken *************")

        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful)
            {
                LoginManager.getInstance().logOut()
                val user = auth.currentUser!!

                MainScope().launch {
                    if (doesUserDocExist(user.uid))
                        loginSuccess()
                    else
                    {
                        FirebaseAuth.getInstance().signOut()
                        startSignUpActivity(SIGN_UP_METHOD_FB, token)
                    }
                }

            } else
            {
                LoginManager.getInstance().logOut()
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInWithCredential:failure", task.exception)
//                Snackbar.make(btnLogin, "Signin Failed", Snackbar.LENGTH_LONG).show()
                Snackbar.make(btnLogin, "Failed: " + task.exception?.message, Snackbar.LENGTH_LONG)
                    .show()
            }
        }
    }


    private fun loginSuccess()
    {
        UserRepository.loggedIn(true)
        navigateToHome()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        Log.d(TAG, "*********** onActivityResult *************")

        callbackManager.onActivityResult(requestCode, resultCode, data) //fb

        super.onActivityResult(requestCode, resultCode, data)

        //google
        if (requestCode == GOOGLE_SIGN_IN)
        {
            val googleSignInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            try
            {
                val account = googleSignInAccountTask.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!) // idToken wont be null because we configured request id token. see documentation for getIdToken.
            } catch (e: ApiException)
            {
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }


    fun hideKeyboard(activity: Activity)
    {
        val imm: InputMethodManager =
            activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view: View? = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null)
        {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
    }

    suspend fun doesUserDocExist(userId: String): Boolean = withContext(IO)
    {
        val docRef = Firebase.firestore.document("/users/${userId}")
        val userDoc = docRef.get().await()

        userDoc.exists()
    }

    fun startSignUpActivity(signUpMethod: String, token: String?)
    {
        val intent = Intent(this@ActivityLogin, ActivitySignUp::class.java)
        intent.putExtra(SIGN_UP_METHOD, signUpMethod)
        intent.putExtra(TOKEN, token)

        startActivity(intent)
        finish()
    }


}