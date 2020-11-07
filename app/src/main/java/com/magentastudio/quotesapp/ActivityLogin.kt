package com.magentastudio.quotesapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.facebook.AccessToken
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
import kotlinx.android.synthetic.main.activity_login.*

class ActivityLogin : AppCompatActivity() {

    private val TAG = "ActivityLogin"

    private lateinit var auth: FirebaseAuth


    private val GOOGLE_SIGN_IN = 2412;
    private lateinit var mGoogleSignInClient: GoogleSignInClient


    private lateinit var callbackManager: CallbackManager
    private val permissions = listOf("email", "public_profile")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)



        callbackManager = CallbackManager.Factory.create()

        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Log.d(TAG, "*********** LoginManager callback onSuccess *************")
                    Log.d(TAG, "facebook:onSuccess:$loginResult")
                    handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {
                    Log.d(TAG, "facebook:onCancel")
                }

                override fun onError(exception: FacebookException) {
                    Log.d(TAG, "facebook:onError", exception)

                }
            })


        btnLogin.setOnClickListener { signin() }
        btnLoginGoogle.setOnClickListener { signInWithGoogle() }
        btnLoginFacebook.setOnClickListener { signInWithFb() }
        btnSignUp.setOnClickListener { startActivity(Intent(this, ActivitySignUp::class.java)) }
    }


    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        Toast.makeText(
            this,
            "Signed In: " + if (currentUser != null) "Yes" else "No",
            Toast.LENGTH_LONG
        )
        Log.i(
            TAG,
            "Signed In: " + if (currentUser != null) "Yes" else "No" + " email: " + currentUser?.email
        )
    }

    fun updateUI(user: FirebaseUser?) {
        user?.run {
            Log.i(TAG, "Signed In:  Yes,  email: " + email +  " profile: " + photoUrl)
            startActivity(
                Intent(this@ActivityLogin, ActivityMain::class.java)
            )
        }
    }


    /**
     * Signin to firebase with email/password
     */
    fun signin() {
        val email = etEmail.text.toString();
        val password = etPassword.text.toString();

        if (email.isEmpty() || password.isEmpty()) {
            Snackbar.make(
                btnLogin.rootView,
                "Please, enter email and password",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                Log.i(TAG, "signIn:success")

                updateUI(user)
            } else {
                Snackbar.make(btnLogin.rootView, "" + task.exception?.message, Snackbar.LENGTH_LONG)
                    .show()
                Log.w(TAG, "signIn:failed " + task.exception)

                updateUI(null)
            }
        }
    }


    fun signInWithGoogle() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser

                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Snackbar.make(
                        btnLogin.rootView,
                        "Authentication Failed.",
                        Snackbar.LENGTH_SHORT
                    ).show()

                    updateUI(null)
                }
            }
    }


    fun signInWithFb() {
        Log.d(TAG, "*********** signInWithFb *************")
        LoginManager.getInstance().logIn(this, permissions)
    }

    private fun handleFacebookAccessToken(accessToken: AccessToken) {
        Log.d(TAG, "*********** handleFacebookAccessToken *************")

        Log.d(TAG, "handleFacebookAccessToken:$accessToken")

        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInWithCredential:success")
                val user = auth.currentUser

                updateUI(user)
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInWithCredential:failure", task.exception)
                Snackbar.make(btnLogin.rootView, "Authentication Failed.", Snackbar.LENGTH_SHORT)
                    .show()

                updateUI(null)
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "*********** onActivityResult *************")

        callbackManager.onActivityResult(requestCode, resultCode, data) //fb

        super.onActivityResult(requestCode, resultCode, data)

        //google
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId())
                firebaseAuthWithGoogle(account.getIdToken()!!) // see documentation for getIdToken.
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

}