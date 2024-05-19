package me.blog.korn123.easydiary.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BaseDevViewModel : ViewModel()  {
    val symbol: MutableLiveData<Int> = MutableLiveData(1)
    val locationInfo: MutableLiveData<String> = MutableLiveData("N/A")
    val coroutine1Console: MutableLiveData<String> = MutableLiveData("N/A")

    fun plus() {
        // Launch a coroutine that reads from a remote data source and updates cache
        viewModelScope.launch {

            // Force Main thread
            withContext(Dispatchers.Main) {
                symbol.value = symbol.value?.plus(1) ?: 1
            }
        }
    }
}