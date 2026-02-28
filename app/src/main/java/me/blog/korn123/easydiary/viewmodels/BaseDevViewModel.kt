package me.blog.korn123.easydiary.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import me.blog.korn123.easydiary.extensions.config

class BaseDevViewModel(
    application: Application,
) : AndroidViewModel(application) {
    val config = application.config
    var symbol by mutableIntStateOf(1)
    var locationInfo by mutableStateOf("N/A")
    var coroutine1Console by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var profilePicUri by mutableStateOf<String?>(null)

    fun plus() {
        symbol = symbol.plus(1)
    }

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

    var enableDebugOptionVisibleTemporaryDiary by mutableStateOf(config.enableDebugOptionVisibleTemporaryDiary)
        private set

    fun toggleDebugOptionVisibleTemporaryDiary() {
        val newValue = enableDebugOptionVisibleTemporaryDiary.not()
        config.enableDebugOptionVisibleTemporaryDiary = newValue
        enableDebugOptionVisibleTemporaryDiary = newValue
    }

    var enableDebugOptionVisibleFontPreviewEmoji by mutableStateOf(config.enableDebugOptionVisibleFontPreviewEmoji)
        private set

    fun toggleDebugOptionVisibleFontPreviewEmoji() {
        val newValue = enableDebugOptionVisibleFontPreviewEmoji.not()
        config.enableDebugOptionVisibleFontPreviewEmoji = newValue
        enableDebugOptionVisibleFontPreviewEmoji = newValue
    }
}
