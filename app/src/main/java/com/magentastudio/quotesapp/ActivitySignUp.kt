package com.magentastudio.quotesapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.btnLogin
import kotlinx.android.synthetic.main.activity_login.btnSignUp
import kotlinx.android.synthetic.main.activity_login.etEmail
import kotlinx.android.synthetic.main.activity_login.etPassword
import kotlinx.android.synthetic.main.activity_sign_up.*

class ActivitySignUp : AppCompatActivity()
{
    private val TAG = "ActivitySignUp"

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        auth = FirebaseAuth.getInstance()

        btnSignUp.setOnClickListener { signUp() }
        btnLogin.setOnClickListener { finish() }
    }

    fun signUp()
    {
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful)
                {
                    Log.d(TAG, "createUserWithEmail:success")
                    Toast.makeText(baseContext, "Account creation successful", Toast.LENGTH_SHORT)
                        .show()
                    val user = auth.currentUser

                    createUserDoc(task)

                    user?.updateProfile(
                        UserProfileChangeRequest.Builder().setDisplayName(etName.text.toString())
                            .build()
                    )
                    updateUI(user)
                }
                else
                {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, task.exception?.message, Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?)
    {
        user?.let {
            Log.i(TAG, "email: ${it.email} \nname: ${it.displayName} ")
            finish()
        }
    }

    /**
     * If this is the first time the user is signing in then
     * create a new empty user document in users collection
     * for this user. The ID of this doc==user's Id
     */
    fun createUserDoc(task: Task<AuthResult>)
    {
        if (task.result!!.additionalUserInfo!!.isNewUser)
        {
            val db = Firebase.firestore
            val userId = auth.currentUser!!.uid

            db.document("/users/${userId}").set(com.magentastudio.quotesapp.Model.User())
        }
    }


}