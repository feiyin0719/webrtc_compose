package com.iffly.compose.redux

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.*
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class StoreViewModel(val list: List<Reducer<Any, Any>>) : ViewModel() {
    private val _reducerMap = mutableMapOf<Class<*>, Reducer<Any, Any>>()
    private val _stateMap = mutableMapOf<Any, MutableLiveData<Any>>()

    init {
        list.forEach {
            _reducerMap[it.actionClass] = it
            _stateMap[it.stateClass] = MutableLiveData(it.initState())
        }
    }

    fun dispatch(action: Any) {
        viewModelScope.launch {
            val reducer = _reducerMap[action::class.java]
            reducer?.let {
                val state = _stateMap[it.stateClass]
                state?.let { _state ->
                    _state.value?.let { _value ->
                        val newState =
                            viewModelScope.async { it.reduce(_value, action = action) }.await()
                        _state.postValue(newState)
                    }
                }
            }
        }

    }

    fun <T> getState(stateClass: Class<T>): MutableLiveData<T> {
        return _stateMap[stateClass]!! as MutableLiveData<T>
    }
}


abstract class Reducer<S, A>(val stateClass: Class<S>, val actionClass: Class<A>) {
    abstract suspend fun reduce(state: S, action: A): S

    abstract fun initState(): S

}


class StoreViewModelFactory(val list: List<Reducer<out Any, out Any>>?) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(modelClass)) {
            return StoreViewModel(list = list!! as List<Reducer<Any, Any>>) as T
        }
        throw RuntimeException("unknown class:" + modelClass.name)
    }

}

@Composable
public fun storeViewModel(
    list: List<Reducer<out Any, out Any>>? = null,
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalContext.current as ViewModelStoreOwner) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    }
): StoreViewModel = viewModelStoreOwner.get(
    StoreViewModel::class.java,
    factory = StoreViewModelFactory(list = list)
)


private fun <VM : ViewModel> ViewModelStoreOwner.get(
    javaClass: Class<VM>,
    key: String? = null,
    factory: ViewModelProvider.Factory? = null
): VM {
    val provider = if (factory != null) {
        ViewModelProvider(this, factory)
    } else {
        ViewModelProvider(this)
    }
    return if (key != null) {
        provider.get(key, javaClass)
    } else {
        provider.get(javaClass)
    }
}
