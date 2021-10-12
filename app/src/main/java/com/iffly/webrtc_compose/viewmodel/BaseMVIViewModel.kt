package com.iffly.webrtc_compose.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class BaseMVIViewModel<S, A>(val stateClass: Class<S>) : ViewModel() {
    private val _userIntent = Channel<A>(Channel.UNLIMITED)
    private val _sharedFlow: SharedFlow<S> = handleAction()
    val viewState: LiveData<S> = _sharedFlow.asLiveData()


    private fun handleAction() =
        _userIntent.receiveAsFlow().map {
            reduce(it, viewState.value ?: stateClass.newInstance())
        }.shareIn(viewModelScope, SharingStarted.Lazily, 1)

    abstract suspend fun reduce(action: A, state: S): S


    fun sendAction(action: A, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            _userIntent.send(action)
        }
    }

    fun sendAction(action: A) {
        sendAction(action = action, viewModelScope)
    }
}