package me.blog.korn123.easydiary.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BaseDevViewModel : ViewModel()  {
    val currentNumber: MutableLiveData<Int> = MutableLiveData(0)

    fun plus() {
        // Launch a coroutine that reads from a remote data source and updates cache
        viewModelScope.launch {

            // Force Main thread
            withContext(Dispatchers.Main) {
                currentNumber.value = currentNumber.value?.plus(1) ?: 1
            }
        }
    }
}