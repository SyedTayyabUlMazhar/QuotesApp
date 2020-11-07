package com.magentastudio.quotesapp

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.annotation.IdRes
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.btnNewQuote
import kotlinx.android.synthetic.main.activity_main.drawer
import kotlinx.android.synthetic.main.appbar.*
import kotlinx.android.synthetic.main.navigation_header.view.*
import kotlinx.android.synthetic.main.navigation_view.*
import kotlinx.android.synthetic.main.navigation_view.view.*

@SuppressLint("RtlHardcoded")
class ActivityMain : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar.setNavigationOnClickListener { drawer.openDrawer(Gravity.LEFT) }

        loadProfilePictureAndName()
        navigationView.setNavigationItemSelectedListener { selectedItem(it.itemId) }

        navigationView.setCheckedItem(R.id.home) //home screen is shown first
        selectedItem(R.id.home)

        ConfirmationDialog(this, "Are you sure you want to logout?").apply {
            dialogButtonClickListener = object : ConfirmationDialog.DialogButtonClickListener {
                override fun yes() {
                    finish()
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this@ActivityMain, ActivityLogin::class.java))
                }
            }
            navigationView.btnLogout.setOnClickListener { show() }
        }

    }

    fun selectedItem(@IdRes id: Int): Boolean {
        return when (id) {
            R.id.home -> {
                toolbar.setTitle("Home")
                drawer.closeDrawer(Gravity.LEFT)
                supportFragmentManager.beginTransaction()
                    .replace(fragmentContainer.id, FragmentHome.newInstance("", ""))
                    .commit()

                btnNewQuote.visibility = View.VISIBLE
                btnNewQuote.setOnClickListener {
                    startActivity(Intent(this, ActivityNewQuote::class.java))
                }
                true
            }
            R.id.favorites -> {
                toolbar.setTitle("Favorites")
                drawer.closeDrawer(Gravity.LEFT)
                supportFragmentManager.beginTransaction()
                    .replace(fragmentContainer.id, FragmentFavorites.newInstance("", ""))
                    .commit()

                btnNewQuote.visibility = View.GONE
                true
            }
            R.id.myQuotes -> {
                drawer.closeDrawer(Gravity.LEFT)
                toolbar.setTitle("My Quotes")
                supportFragmentManager.beginTransaction()
                    .replace(fragmentContainer.id, FragmentMyQuotes.newInstance("", ""))
                    .commit()

                btnNewQuote.visibility = View.GONE
                true
            }
            else -> false
        }
    }

    fun loadProfilePictureAndName() {
        val user = FirebaseAuth.getInstance().currentUser!!
        user.photoUrl?.let { url ->
            Glide
                .with(this)
                .load(url)
                .centerCrop()
                .into(navigationView.getHeaderView(0).ivProfilePicture)
        }
        navigationView.getHeaderView(0).tvUserName.setText(user.displayName)

    }
}