package com.magentastudio.quotesapp.UI.Common

import android.app.Activity
import android.content.Context
import android.media.Image
import android.net.Uri
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.magentastudio.quotesapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.reflect.KMutableProperty0

suspend fun Context.showToastMainCoroutine(message: String) = withContext(Dispatchers.Main)
{
    Toast.makeText(this@showToastMainCoroutine, message, Toast.LENGTH_LONG).show()
}

fun Context.showToast(message: String)
{
    Toast.makeText(this@showToast, message, Toast.LENGTH_LONG).show()
}

/**
 * an extension to flip boolean
 */
fun KMutableProperty0<Boolean>.flip() = set(!get())


fun ImageView.setTint(@ColorRes color: Int)
{
    setColorFilter(
            resources.getColor(color), android.graphics.PorterDuff.Mode.SRC_IN
    )
}

/**
 * converts image path to storage reference
 */
fun String.toStorageRef() = Firebase.storage.reference.child(this)


fun Activity.loadImage(imagePath: String, imageView: ImageView)
{
    val imageRef = imagePath.toStorageRef()

    Glide.with(this).load(imageRef).error(R.drawable.avatar)
            .into(imageView)
}

fun Context.loadImage(imagePath: String, imageView: ImageView)
{
    val imageRef = imagePath.toStorageRef()

    Glide.with(this).load(imageRef).error(R.drawable.avatar)
            .into(imageView)
}

fun Fragment.loadImage(imagePath: String, imageView: ImageView)
{
    val imageRef = imagePath.toStorageRef()

    Glide.with(this).load(imageRef).error(R.drawable.avatar)
            .into(imageView)
}

fun Fragment.loadImage(uri: Uri, imageView: ImageView)
{

    Glide.with(this).load(uri).error(R.drawable.avatar)
            .into(imageView)
}