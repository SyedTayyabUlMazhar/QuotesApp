package com.magentastudio.quotesapp

import ConfirmationDialog
import ConfirmationDialog.DialogButtonClickListener
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.core.view.GravityCompat
import com.magentastudio.quotesapp.Model.Quote
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.appbar.*
import kotlinx.android.synthetic.main.navigation_view.*
import kotlinx.android.synthetic.main.navigation_view.view.*

class ActivityHome : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)


        toolbar.setNavigationOnClickListener { drawer.openDrawer(GravityCompat.START) }

        listOf(
            Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote(), Quote()
        ).let {
            rv_quotes.adapter = QuoteAdapter(this, it)
        }

        btnNewQuote.setOnClickListener { startActivity(Intent(this, ActivityNewQuote::class.java)) }

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
        navigationView.setCheckedItem(R.id.home)


        ConfirmationDialog(this, "Are you sure you want to logout?").apply {
            dialogButtonClickListener = object : DialogButtonClickListener {
                override fun yes() {
                    finish()
                    startActivity(Intent(this@ActivityHome, ActivityLogin::class.java))
                }
            }
            navigationView.btnLogout.setOnClickListener { show() }
        }


    }
}