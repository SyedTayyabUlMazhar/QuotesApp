package com.magentastudio.quotesapp

import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.android.synthetic.main.activity_base.drawer
import kotlinx.android.synthetic.main.appbar.*
import kotlinx.android.synthetic.main.navigation_view.*
import kotlinx.android.synthetic.main.navigation_view.view.*

abstract class ActivityBase : AppCompatActivity() {

    override fun setContentView(layoutResID: Int) {
        Log.d("BASE", "setContentView")
        super.setContentView(R.layout.activity_base)
        layoutContainer.addView(layoutInflater.inflate(layoutResID, null))

        toolbar.setNavigationOnClickListener { drawer.openDrawer(GravityCompat.START) }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, ActivityHome::class.java))
                    true
                }
                R.id.favorites -> {
                    startActivity(Intent(this, ActivityFavorites::class.java))
                    true
                }
                R.id.myQuotes -> {
                    startActivity(Intent(this, ActivityMyQuotes::class.java))
                    true
                }

                else -> false
            }
        }

        ConfirmationDialog(this, "Are you sure you want to logout?").apply {
            dialogButtonClickListener = object : ConfirmationDialog.DialogButtonClickListener {
                override fun yes() {
                    finish()
                    startActivity(Intent(this@ActivityBase, ActivityLogin::class.java))
                }
            }
            navigationView.btnLogout.setOnClickListener { show() }
        }
    }

    override fun setTitle(title: CharSequence?) {
        toolbar.setTitle(title)
    }

    fun setSelectedNavigationItem(menuItemId: Int) {
        navigationView.menu.findItem(menuItemId)?.let {
            navigationView.setCheckedItem(it)
        }
    }
}