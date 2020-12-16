package com.magentastudio.quotesapp

sealed class Response<out T>
{
    data class Default<out R>(val default: R) : Response<R>()
    data class Success<out R>(val result: R) : Response<R>()
    data class Failure(val message: String) : Response<Nothing>()
    object Loading: Response<Nothing>()
}