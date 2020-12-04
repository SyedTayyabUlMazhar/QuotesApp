package com.magentastudio.quotesapp.UI.Common

import android.content.Context
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.ColorRes
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
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

fun String.toStorageReference(): StorageReference
{
    return Firebase.storage.getReferenceFromUrl(this)
}
