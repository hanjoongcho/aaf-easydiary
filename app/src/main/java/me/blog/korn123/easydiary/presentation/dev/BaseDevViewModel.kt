package me.blog.korn123.easydiary.presentation.dev

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.blog.korn123.easydiary.domain.model.ActionLog
import me.blog.korn123.easydiary.domain.model.Alarm
import me.blog.korn123.easydiary.domain.model.DDay
import me.blog.korn123.easydiary.domain.repository.ActionLogRepository
import me.blog.korn123.easydiary.domain.repository.AlarmRepository
import me.blog.korn123.easydiary.domain.repository.DDayRepository
import me.blog.korn123.easydiary.extensions.config
import javax.inject.Inject

@HiltViewModel
class BaseDevViewModel
    @Inject
    constructor(
        application: Application,
        private val alarmRepository: AlarmRepository,
        private val actionLogRepository: ActionLogRepository,
        private val dDayRepository: DDayRepository,
    ) : AndroidViewModel(application) {
        val config = application.config
        var symbol by mutableIntStateOf(1)
        var locationInfo by mutableStateOf("N/A")
        var coroutine1Console by mutableStateOf("")
        var isLoading by mutableStateOf(false)
        var loadingMessage by mutableStateOf<String?>(null)
        var profilePicUri by mutableStateOf<String?>(null)

        fun plus() {
            symbol = symbol.plus(1)
        }

        fun addAlarm(alarm: Alarm) {
            viewModelScope.launch {
                alarmRepository.insertAlarm(alarm)
            }
        }
    }
