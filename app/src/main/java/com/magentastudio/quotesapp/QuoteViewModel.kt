package com.magentastudio.quotesapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.magentastudio.quotesapp.Model.Quote
import com.magentastudio.quotesapp.Model.UserData
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class QuoteViewModel : ViewModel()
{
    private val TAG = "QuoteViewModel"
    private val EMPTY_LIST = listOf<Quote>()

    private val repository = QuoteRepository()
    private val userRepo = UserRepository()


    private var _quotesOnly = MutableStateFlow<Response<List<Quote>>>(Response.Default(EMPTY_LIST))
    private var _userData = MutableStateFlow<Response<UserData>>(Response.Default(UserData()))

    private var _processedQuotes =
        MutableStateFlow<Response<List<Quote>>>(Response.Default(EMPTY_LIST))
    val processedQuotes = _processedQuotes.asStateFlow()

    private var _myQuotes = MutableStateFlow<Response<List<Quote>>>(Response.Default(EMPTY_LIST))
    val myQuotes = _myQuotes.asStateFlow()

    private var _favoriteQuotes =
        MutableStateFlow<Response<MutableList<Quote>>>(Response.Default(EMPTY_LIST.toMutableList()))
    val favoriteQuotes = _favoriteQuotes.asStateFlow()

    init
    {
        viewModelScope.launch {
            Log.i(TAG, "-----------initializing _userData--------")
            loadUserData()
            Log.i(TAG, "-----------initialized _userData--------")

            Log.i(TAG, "-----------initializing _quotesOnly--------")
            loadPlainQuotes()
            Log.i(TAG, "-----------initialized _quotesOnly--------")

            Log.i(TAG, "-----------initializing _processedQuotes--------")
            launch { loadProcessedQuotes() }
            Log.i(TAG, "-----------initialized _processedQuotes--------")

            Log.i(TAG, "-----------initializing _myQuotes--------")
            launch { loadMyQuotes() }
            Log.i(TAG, "-----------initialized _myQuotes--------")

            Log.i(TAG, "-----------initializing _favoriteQuotes--------")
            launch { loadFavoriteQuotes() }
            Log.i(TAG, "-----------initialized _favoriteQuotes--------")
        }
    }


    private suspend fun loadUserData()
    {
        UserRepository.userData
            .filter { it !is Response.Default }
            .take(1)
            .collect { _userData.value = it }
    }

    private suspend fun loadPlainQuotes() = withContext(IO)
    {
        try
        {
            _quotesOnly.value = Response.Success(repository.fetchQuotes())
        } catch (e: Exception)
        {
            _quotesOnly.value = Response.Failure("$e")
        }
    }

    private suspend fun loadProcessedQuotes() = withContext(Default)
    {
        _processedQuotes.value = Response.Loading

        _userData.combine(_quotesOnly)
        { userDataResponse, quotesResponse ->
            if (userDataResponse is Response.Failure || quotesResponse is Response.Failure)
            {
                Log.i(TAG, "loadProcessedQuotes() : Response:Failure")
                Response.Failure("Error: Loading Processed Quotes")
            } else if (userDataResponse is Response.Default || quotesResponse is Response.Default)
            {
                Log.i(TAG, "loadProcessedQuotes() : Response:Default")
                Response.Default(EMPTY_LIST)
            } else
            {
                Log.i(TAG, "loadProcessedQuotes() : Response:Success")

                userDataResponse as Response.Success
                quotesResponse as Response.Success

                quotesResponse.result.onEach { userDataResponse.result.setUpQuote(it) }

                quotesResponse
            }
        }.collect {
            Log.i(TAG, "loadProcessedQuotes() Collecting")
            _processedQuotes.value = it
        }
    }

    private suspend fun loadMyQuotes() = withContext(Default)
    {
        _userData.combine(_processedQuotes)
        { userDataResponse, quotesResponse ->
            if (userDataResponse is Response.Failure || quotesResponse is Response.Failure)
            {
                Log.i(TAG, "loadMyQuotes() : Response:Failure")
                Response.Failure("Error: Loading My Quotes")
            } else if (userDataResponse is Response.Default || quotesResponse is Response.Default)
            {
                Log.i(TAG, "loadMyQuotes() : Response:Default")
                Response.Default(EMPTY_LIST)
            } else if (userDataResponse is Response.Loading || quotesResponse is Response.Loading)
            {
                Response.Loading
            } else
            {
                Log.i(TAG, "loadMyQuotes() : Response:Success")

                userDataResponse as Response.Success
                quotesResponse as Response.Success

                val myQuotes = quotesResponse.result.filter {
                    it.user["id"] == UserRepository.userId
                }

                Response.Success(myQuotes)
            }
        }.collect {
            Log.i(TAG, "loadMyQuotes() Collecting")
            _myQuotes.value = it
        }
    }

    private suspend fun loadFavoriteQuotes() = withContext(Default)
    {
        _userData.combine(_processedQuotes)
        { userDataResponse, quotesResponse ->
            if (userDataResponse is Response.Failure || quotesResponse is Response.Failure)
            {
                Log.i(TAG, "loadFavoriteQuotes() : Response:Failure")
                Response.Failure("Error: Loading Favorite Quotes")
            } else if (userDataResponse is Response.Default || quotesResponse is Response.Default)
            {
                Log.i(TAG, "loadFavoriteQuotes() : Response:Default")
                Response.Default(EMPTY_LIST)
            } else if (userDataResponse is Response.Loading || quotesResponse is Response.Loading)
            {
                Response.Loading
            } else
            {
                Log.i(TAG, "loadFavoriteQuotes() : Response:Success")

                userDataResponse as Response.Success
                quotesResponse as Response.Success

                val favoriteQuotesIds = userDataResponse.result.favorites

                val favoriteQuotes = quotesResponse.result.filter {
                    favoriteQuotesIds.contains(it.docId)
                }

                Response.Success(favoriteQuotes)
            }
        }.collect {
            Log.i(TAG, "loadFavoriteQuotes() Collecting")

            _favoriteQuotes.value = it as Response<MutableList<Quote>>
        }
    }

    suspend fun refresh()
    {
        loadPlainQuotes()
        loadUserData()
    }

    fun favorite(quote: Quote)
    {
        repository.favorite(quote)

        quote.favorited = true
        val favoriteQuotes = (_favoriteQuotes.value as Response.Success).result
        favoriteQuotes.add(quote)
    }

    fun unfavorite(quote: Quote)
    {
        repository.unfavorite(quote)

        quote.favorited = false
        val favoriteQuotes = (_favoriteQuotes.value as Response.Success).result
        favoriteQuotes.remove(quote)
    }

    fun toggleUpvote(quote: Quote)
    {
        repository.toggleUpvote(quote)
    }

    fun toggleDownvote(quote: Quote)
    {
        repository.toggleDownvote(quote)
    }

}