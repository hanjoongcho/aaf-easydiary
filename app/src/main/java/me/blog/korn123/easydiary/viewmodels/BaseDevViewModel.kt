package me.blog.korn123.easydiary.viewmodels

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

        suspend fun addAllAlarms(alarms: List<Alarm>): Int {
            var count = 0
            loadingMessage = "Alarm migration started..."

            try {
                withContext(Dispatchers.IO) {
                    alarms.forEachIndexed { index, alarm ->
                        alarmRepository.insertAlarm(alarm)
                        if (index % 10 == 0 || index == alarms.lastIndex) {
                            withContext(Dispatchers.Main) {
                                loadingMessage = "Migrating alarms... ${index + 1} / ${alarms.size}"
                            }
                        }
                        count++
                    }
                }
                loadingMessage = "Alarm migration completed: ${alarms.size} alarms\n"
            } catch (e: Exception) {
                loadingMessage = "Alarm migration failed: ${e.message}\n"
            } finally {
                loadingMessage = "Alarm migration completed: $count alarms\n"
            }

            return count
        }

        fun updateAlarm(alarm: Alarm) {
            viewModelScope.launch {
                alarmRepository.updateAlarm(alarm)
            }
        }

        fun deleteAlarm(alarm: Alarm) {
            viewModelScope.launch {
                alarmRepository.deleteAlarm(alarm)
            }
        }

        fun deleteAllAlarms() {
            viewModelScope.launch {
                alarmRepository.deleteAllAlarms()
            }
        }

        suspend fun getAlarmCount(): Int = alarmRepository.getAllAlarms().first().size

        suspend fun addAllActionLogs(actionLogs: List<ActionLog>): Int {
            var count = 0
            loadingMessage = "Action log migration started..."

            try {
                withContext(Dispatchers.IO) {
                    actionLogs.forEachIndexed { index, log ->
                        actionLogRepository.insertActionLog(log)
                        if (index % 10 == 0 || index == actionLogs.lastIndex) {
                            withContext(Dispatchers.Main) {
                                loadingMessage = "Migrating action logs... ${index + 1} / ${actionLogs.size}"
                            }
                        }
                        count++
                    }
                }
                loadingMessage = "Action log migration completed: ${actionLogs.size} logs\n"
            } catch (e: Exception) {
                loadingMessage = "Action log migration failed: ${e.message}\n"
            } finally {
                loadingMessage = "Action log migration completed: $count logs\n"
            }

            return count
        }

        fun deleteAllActionLogs() {
            viewModelScope.launch {
                actionLogRepository.deleteAllActionLogs()
            }
        }

        suspend fun getActionLogCount(): Int = actionLogRepository.getAllActionLogs().first().size

        suspend fun addAllDDays(dDays: List<DDay>): Int {
            var count = 0
            loadingMessage = "D-day migration started..."

            try {
                withContext(Dispatchers.IO) {
                    dDays.forEachIndexed { index, dDay ->
                        dDayRepository.insertDDay(dDay)
                        if (index % 10 == 0 || index == dDays.lastIndex) {
                            withContext(Dispatchers.Main) {
                                loadingMessage = "Migrating d-days... ${index + 1} / ${dDays.size}"
                            }
                        }
                        count++
                    }
                }
                loadingMessage = "D-day migration completed: ${dDays.size} d-days\n"
            } catch (e: Exception) {
                loadingMessage = "D-day migration failed: ${e.message}\n"
            } finally {
                loadingMessage = "D-day migration completed: $count d-days\n"
            }

            return count
        }

        fun deleteAllDDays() {
            viewModelScope.launch {
                dDayRepository.deleteAllDDays()
            }
        }

        suspend fun getDDayCount(): Int = dDayRepository.getAllDDays().first().size

        var enableJetpackRoomDatabase by mutableStateOf(config.enableJetpackRoomDatabase)
            private set

        fun toggleEnableJetpackRoomDatabase() {
            val newValue = enableJetpackRoomDatabase.not()
            config.enableJetpackRoomDatabase = newValue
            enableJetpackRoomDatabase = newValue
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
