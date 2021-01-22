package com.magentastudio.quotesapp.UI.Screen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.magentastudio.quotesapp.R
import com.magentastudio.quotesapp.Response
import com.magentastudio.quotesapp.UI.Common.ConfirmationDialog
import com.magentastudio.quotesapp.UI.Common.loadImage
import com.magentastudio.quotesapp.UserRepository
import com.magentastudio.quotesapp.databinding.ActivityMainBinding
import com.magentastudio.quotesapp.databinding.NavigationHeaderBinding
import com.magentastudio.quotesapp.databinding.NavigationViewBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ActivityMain : AppCompatActivity()
{
    private val TAG = "ActivityMain"

    private lateinit var binding: ActivityMainBinding
    private lateinit var navViewBinding: NavigationViewBinding

    private val KEY_SELECTED_ITEM_ID = "KEY_SELECTED_ITEM_ID"
    private var selectedItemId = 0

    private val KEY_CURRENT_FRAGMENT = "CURRENT_FRAGMENT"
    private lateinit var currentFragment: Fragment

    @SuppressLint("RtlHardcoded")
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navViewBinding = binding.incNavView

        binding.appbar.toolbar.setNavigationOnClickListener { binding.drawer.openDrawer(Gravity.LEFT) }
        navViewBinding.navigationView.setNavigationItemSelectedListener { selectItem(it.itemId, null) }

        loadProfile()

        binding.btnNewQuote.setOnClickListener { openNewQuoteFragment() }

        if (savedInstanceState == null)
        {
            selectItem(R.id.home, null)
            navViewBinding.navigationView.setCheckedItem(R.id.home)
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
//            navigationView.btnLogout.setOnClickListener { show() }
            navViewBinding.btnLogout.setOnClickListener { show() }
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

    @SuppressLint("RtlHardcoded")
    private fun selectItem(@IdRes id: Int, fragment: Fragment?): Boolean
    {
//        drawer.closeDrawer(Gravity.LEFT)

        if (selectedItemId == id) return false

        binding.appbar.toolbar.title = navViewBinding.navigationView.menu.findItem(id).title
        binding.btnNewQuote.visibility = View.GONE

        selectedItemId = id

        when (id)
        {
            R.id.home ->
            {
                currentFragment = fragment ?: FragmentHome()

                binding.btnNewQuote.visibility = View.VISIBLE
//                btnNewQuote.setOnClickListener { openNewQuoteFragment() }
            }
            R.id.favorites -> currentFragment = fragment ?: FragmentFavorites()
            R.id.myQuotes -> currentFragment = fragment ?: FragmentMyQuotes()
            R.id.setting -> currentFragment = fragment ?: FragmentSetting()
        }

        supportFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, currentFragment)
                .commit()


        MainScope().launch {
            delay(150)
            binding.drawer.closeDrawer(Gravity.LEFT)
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
                if (it is Response.Success)
                {
                    val userData = it.result
                    val navHeaderBinding = NavigationHeaderBinding.bind(navViewBinding.navigationView.getHeaderView(0))

                    navHeaderBinding.tvUserName.setText(userData.name)
                    loadImage(userData.profilePicPath, navHeaderBinding.ivProfilePicture)
                }
            }
        }

    }

    private fun openNewQuoteFragment()
    {
        supportFragmentManager.beginTransaction().replace(
                android.R.id.content,
                FragmentNewQuote()
        ).commit()
    }

    fun quoteAdded()
    {
        (currentFragment as FragmentHome).quoteAdded()
    }
}