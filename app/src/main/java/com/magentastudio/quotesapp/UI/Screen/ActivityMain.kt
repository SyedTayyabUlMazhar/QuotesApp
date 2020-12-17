package com.magentastudio.quotesapp.UI.Screen

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.magentastudio.quotesapp.Model.UserData
import com.magentastudio.quotesapp.R
import com.magentastudio.quotesapp.UI.Common.ConfirmationDialog
import com.magentastudio.quotesapp.UserRepository
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.btnNewQuote
import kotlinx.android.synthetic.main.activity_main.drawer
import kotlinx.android.synthetic.main.appbar.*
import kotlinx.android.synthetic.main.navigation_header.view.*
import kotlinx.android.synthetic.main.navigation_view.*
import kotlinx.android.synthetic.main.navigation_view.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("RtlHardcoded")
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

        loadProfilePictureAndName()

        navigationView.setNavigationItemSelectedListener { selectItem(it.itemId, null) }


        if (savedInstanceState == null)
        {
            selectItem(R.id.home, null)
            navigationView.setCheckedItem(R.id.home)
        }
        else savedInstanceState.run {
            val previouslySelectedItemId = getInt(KEY_SELECTED_ITEM_ID)
            val previouslyCommittedFragment =
                    supportFragmentManager.getFragment(this, KEY_CURRENT_FRAGMENT)

            selectItem(previouslySelectedItemId, previouslyCommittedFragment)
        }

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

    override fun onSaveInstanceState(outState: Bundle)
    {
        super.onSaveInstanceState(outState)
        supportFragmentManager.putFragment(outState, KEY_CURRENT_FRAGMENT, currentFragment)
        outState.putInt(KEY_SELECTED_ITEM_ID, selectedItemId)
    }

    private fun logout()
    {
        UserRepository.loggedIn(true)
        FirebaseAuth.getInstance().signOut()
//        LoginManager.getInstance().logOut()
//        GoogleSignIn.getClient(applicationContext, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
    }

    fun selectItem(@IdRes id: Int): Boolean
    {
        toolbar.title = navigationView.menu.findItem(id).title
        drawer.closeDrawer(Gravity.LEFT)
        btnNewQuote.visibility = View.GONE

        selectedItemId = id

        var fragment: Fragment? = null
        when (id)
        {
            R.id.home ->
            {
                fragment = FragmentHome()

                btnNewQuote.visibility = View.VISIBLE
                btnNewQuote.setOnClickListener()
                {
                    startActivity(Intent(this, ActivityNewQuote::class.java))
                }
            }
            R.id.favorites -> fragment = FragmentFavorites()
            R.id.myQuotes -> fragment = FragmentMyQuotes()
            R.id.setting -> fragment = FragmentSetting()
        }

        supportFragmentManager.beginTransaction()
                .replace(fragmentContainer.id, fragment!!)
                .commit()

        return true
    }

    fun selectItem(@IdRes id: Int, fragment: Fragment?): Boolean
    {
        drawer.closeDrawer(Gravity.LEFT)

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
                btnNewQuote.setOnClickListener()
                {
                    startActivity(Intent(this, ActivityNewQuote::class.java))
                }
            }
            R.id.favorites -> currentFragment = fragment ?: FragmentFavorites()
            R.id.myQuotes -> currentFragment = fragment ?: FragmentMyQuotes()
            R.id.setting -> currentFragment = fragment ?: FragmentSetting()
        }

        supportFragmentManager.beginTransaction()
                .replace(fragmentContainer.id, currentFragment)
                .commit()

        return true
    }

    fun loadProfilePictureAndName()
    {
//        CoroutineScope(IO).launch()
//        {
//            val userId = FirebaseAuth.getInstance().currentUser!!.uid
//            val user = Firebase.firestore.document("/users/$userId").get()
//                    .await().toObject<User>()!!
//
//            withContext(Main) { navigationView.tvUserName.setText(user.name) }
//
//            val imageRef = Firebase.storage.getReferenceFromUrl(user.profilePic)
//            withContext(Main) {
//                if (!this@ActivityMain.isDestroyed)
//                    Glide.with(this@ActivityMain).load(imageRef).into(navigationView.iv_profilePicture)
//            }
//        }

        CoroutineScope(IO).launch()
        {
            val user = UserData.get()
//            val imageRef = user.profilePicReference()
            val imageRef = Firebase.storage.reference.child(user.profilePicPath)

            withContext(Main) {
                navigationView.tvUserName.setText(user.name)
                if (!this@ActivityMain.isDestroyed)
                    Glide.with(this@ActivityMain).load(imageRef)
                            .into(navigationView.iv_profilePicture)
            }
        }

    }
}