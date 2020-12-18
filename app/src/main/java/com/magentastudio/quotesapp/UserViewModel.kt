package com.magentastudio.quotesapp

import android.net.Uri
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel()
{
    private val repository = UserRepository()

    fun changeProfilePic(uri: Uri) = repository.changeProfilePic(uri)

    fun changeName(name: String) = repository.changeName(name)
}