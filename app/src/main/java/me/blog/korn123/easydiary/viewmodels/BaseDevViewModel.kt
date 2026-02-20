package me.blog.korn123.easydiary.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.easydiary.extensions.config

class BaseDevViewModel(
    application: Application,
) : AndroidViewModel(application) {
    val config = application.config
    val symbol: MutableLiveData<Int> = MutableLiveData(1)
    val locationInfo: MutableLiveData<String> = MutableLiveData("N/A")
    val coroutine1Console: MutableLiveData<String> = MutableLiveData("")

    fun plus() {
        // Launch a coroutine that reads from a remote data source and updates cache
        viewModelScope.launch {
            // Force Main thread
            withContext(Dispatchers.Main) {
                symbol.value = symbol.value?.plus(1) ?: 1
            }
        }
    }

    var isLoading by mutableStateOf(false)
    var profilePicUri by mutableStateOf<String?>(null)

    var enableDebugOptionVisibleDiarySequence by mutableStateOf(config.enableDebugOptionVisibleDiarySequence)
        private set // We control the internal state

    fun toggleDebugOptionVisibleDiarySequence() {
        val newValue = enableDebugOptionVisibleDiarySequence.not()
        config.enableDebugOptionVisibleDiarySequence = newValue
        enableDebugOptionVisibleDiarySequence = newValue
    }

    var enableDebugOptionVisibleAlarmSequence by mutableStateOf(config.enableDebugOptionVisibleAlarmSequence)
        private set // We control the internal state

    fun toggleDebugOptionVisibleAlarmSequence() {
        val newValue = enableDebugOptionVisibleAlarmSequence.not()
        config.enableDebugOptionVisibleAlarmSequence = newValue
        enableDebugOptionVisibleAlarmSequence = newValue
    }

    var enableDebugOptionVisibleTreeStatus by mutableStateOf(config.enableDebugOptionVisibleTreeStatus)
        private set // We control the internal state

    fun toggleDebugOptionVisibleTreeStatus() {
        val newValue = enableDebugOptionVisibleTreeStatus.not()
        config.enableDebugOptionVisibleTreeStatus = newValue
        enableDebugOptionVisibleTreeStatus = newValue
    }

    var enableDebugOptionVisibleChartStock by mutableStateOf(config.enableDebugOptionVisibleChartStock)
        private set // We control the internal state

    fun toggleDebugOptionVisibleChartStock() {
        val newValue = enableDebugOptionVisibleChartStock.not()
        config.enableDebugOptionVisibleChartStock = newValue
        enableDebugOptionVisibleChartStock = newValue
    }

    var enableDebugOptionVisibleChartWeight by mutableStateOf(config.enableDebugOptionVisibleChartWeight)
        private set // We control the internal state

    fun toggleDebugOptionVisibleChartWeight() {
        val newValue = enableDebugOptionVisibleChartWeight.not()
        config.enableDebugOptionVisibleChartWeight = newValue
        enableDebugOptionVisibleChartWeight = newValue
    }

    var enableDebugOptionToastLocation by mutableStateOf(config.enableDebugOptionToastLocation)
        private set // We control the internal state

    fun toggleDebugOptionToastLocation() {
        val newValue = enableDebugOptionToastLocation.not()
        config.enableDebugOptionToastLocation = newValue
        enableDebugOptionToastLocation = newValue
    }
}
