package com.magentastudio.quotesapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.annotation.IdRes
import com.bumptech.glide.Glide
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthCredential
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.btnNewQuote
import kotlinx.android.synthetic.main.activity_main.drawer
import kotlinx.android.synthetic.main.appbar.*
import kotlinx.android.synthetic.main.navigation_header.view.*
import kotlinx.android.synthetic.main.navigation_view.*
import kotlinx.android.synthetic.main.navigation_view.view.*

@SuppressLint("RtlHardcoded")
class ActivityMain : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar.setNavigationOnClickListener { drawer.openDrawer(Gravity.LEFT) }

        loadProfilePictureAndName()
        navigationView.setNavigationItemSelectedListener { selectedItem(it.itemId) }

        navigationView.setCheckedItem(R.id.setting) //home screen is shown first
        selectedItem(R.id.setting)

        ConfirmationDialog(this, "Are you sure you want to logout?").apply {
            dialogButtonClickListener = object : ConfirmationDialog.DialogButtonClickListener
            {
                override fun yes()
                {
                    finish()
                    logout()
                    startActivity(Intent(this@ActivityMain, ActivityLogin::class.java))
                }
            }
            navigationView.btnLogout.setOnClickListener { show() }
        }

    }

    private fun logout()
    {
        FirebaseAuth.getInstance().signOut()
        LoginManager.getInstance().logOut()
        GoogleSignIn.getClient(applicationContext, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
    }

    fun selectedItem(@IdRes id: Int): Boolean
    {
        return when (id)
        {
            R.id.home ->
            {
                toolbar.title = "Home"
                drawer.closeDrawer(Gravity.LEFT)
                supportFragmentManager.beginTransaction()
                    .replace(fragmentContainer.id, FragmentHome())
                    .commit()

                btnNewQuote.visibility = View.VISIBLE
                btnNewQuote.setOnClickListener {
                    startActivity(Intent(this, ActivityNewQuote::class.java))
                }
                true
            }
            R.id.favorites ->
            {
                toolbar.title = "Favorites"
                drawer.closeDrawer(Gravity.LEFT)
                supportFragmentManager.beginTransaction()
                    .replace(fragmentContainer.id, FragmentFavorites())
                    .commit()

                btnNewQuote.visibility = View.GONE
                true
            }
            R.id.myQuotes ->
            {
                drawer.closeDrawer(Gravity.LEFT)
                toolbar.title = "My Quotes"
                supportFragmentManager.beginTransaction()
                    .replace(fragmentContainer.id, FragmentMyQuotes())
                    .commit()

                btnNewQuote.visibility = View.GONE
                true
            }
            R.id.setting ->
            {
                drawer.closeDrawer(Gravity.LEFT)
                toolbar.title = "Profile"
                supportFragmentManager.beginTransaction()
                    .replace(fragmentContainer.id, FragmentSetting())
                    .commit()

                btnNewQuote.visibility = View.GONE
                true
            }
            else           -> false
        }
    }

    fun loadProfilePictureAndName()
    {
        val user = FirebaseAuth.getInstance().currentUser!!
        user.photoUrl?.let { url ->
            Glide
                .with(this)
                .load(url)
                .centerCrop()
                .into(navigationView.getHeaderView(0).ivProfilePicture)
        }
        navigationView.getHeaderView(0).tvUserName.text = user.displayName

    }
}