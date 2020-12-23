package com.magentastudio.quotesapp.UI.Screen

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.magentastudio.quotesapp.R
import com.magentastudio.quotesapp.Response
import com.magentastudio.quotesapp.UI.Common.ConfirmationDialog
import com.magentastudio.quotesapp.UI.Common.loadImage
import com.magentastudio.quotesapp.UserRepository
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.btnNewQuote
import kotlinx.android.synthetic.main.activity_main.drawer
import kotlinx.android.synthetic.main.appbar.*
import kotlinx.android.synthetic.main.navigation_header.view.*
import kotlinx.android.synthetic.main.navigation_view.*
import kotlinx.android.synthetic.main.navigation_view.view.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ActivityMain : AppCompatActivity()
{
    private val TAG = "ActivityMain"

    private val KEY_SELECTED_ITEM_ID = "KEY_SELECTED_ITEM_ID"
    private var selectedItemId = 0

    private val KEY_CURRENT_FRAGMENT = "CURRENT_FRAGMENT"
    private lateinit var currentFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar.setNavigationOnClickListener { drawer.openDrawer(Gravity.LEFT) }
        navigationView.setNavigationItemSelectedListener { selectItem(it.itemId, null) }

        loadProfile()

        btnNewQuote.setOnClickListener { openNewQuoteFragment() }

        if (savedInstanceState == null)
        {
            selectItem(R.id.home, null)
            navigationView.setCheckedItem(R.id.home)
        }
        else savedInstanceState.run { loadState(this) }

        ConfirmationDialog(this, "Are you sure you want to logout?").apply {
            dialogButtonClickListener = object : ConfirmationDialog.DialogButtonClickListener
            {
                override fun yes()
                {
                    logout()
                    finish()
                    startActivity(Intent(this@ActivityMain, ActivityLogin::class.java))
                }
            }
            navigationView.btnLogout.setOnClickListener { show() }
        }
    }

    override fun onSaveInstanceState(outState: Bundle)
    {
        super.onSaveInstanceState(outState)
        supportFragmentManager.putFragment(outState, KEY_CURRENT_FRAGMENT, currentFragment)
        outState.putInt(KEY_SELECTED_ITEM_ID, selectedItemId)
    }

    private fun loadState(savedInstanceState: Bundle)
    {
        savedInstanceState.run {
            val previouslySelectedItemId = getInt(KEY_SELECTED_ITEM_ID)
            val previouslyCommittedFragment =
                    supportFragmentManager.getFragment(this, KEY_CURRENT_FRAGMENT)

            selectItem(previouslySelectedItemId, previouslyCommittedFragment)
        }
    }

    private fun logout()
    {
        UserRepository.loggedIn(false)
        FirebaseAuth.getInstance().signOut()
    }

    private fun selectItem(@IdRes id: Int, fragment: Fragment?): Boolean
    {
//        drawer.closeDrawer(Gravity.LEFT)

        if (selectedItemId == id) return false

        toolbar.title = navigationView.menu.findItem(id).title
        btnNewQuote.visibility = View.GONE

        selectedItemId = id

        when (id)
        {
            R.id.home ->
            {
                currentFragment = fragment ?: FragmentHome()

                btnNewQuote.visibility = View.VISIBLE
//                btnNewQuote.setOnClickListener { openNewQuoteFragment() }
            }
            R.id.favorites -> currentFragment = fragment ?: FragmentFavorites()
            R.id.myQuotes -> currentFragment = fragment ?: FragmentMyQuotes()
            R.id.setting -> currentFragment = fragment ?: FragmentSetting()
        }

        supportFragmentManager.beginTransaction()
                .replace(fragmentContainer.id, currentFragment)
                .commit()

        MainScope().launch {
            delay(150)
            drawer.closeDrawer(Gravity.LEFT)
        }

        return true
    }


    /**
     * load username and profile picture into header
     * of navigation view
     */
    private fun loadProfile()
    {
        lifecycleScope.launchWhenStarted {
            UserRepository.userData.collect {
                val userData = (it as Response.Success).result

                navigationView.getHeaderView(0).tvUserName.setText(userData.name)
                loadImage(userData.profilePicPath, navigationView.getHeaderView(0).iv_profilePicture)
            }
        }

    }

    private fun openNewQuoteFragment()
    {
        supportFragmentManager.beginTransaction().replace(
                android.R.id.content,
                NewQuoteFragment()
        ).commit()
    }

    fun quoteAdded()
    {
        (currentFragment as FragmentHome).quoteAdded()
    }
}