package me.blog.korn123.easydiary.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.runBlocking
import me.blog.korn123.easydiary.domain.model.Alarm
import me.blog.korn123.easydiary.domain.repository.AlarmRepository
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.AlarmConstants
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel
    @Inject
    constructor(
        application: Application,
        private val alarmRepository: AlarmRepository,
    ) : AndroidViewModel(application) {
        /***************************************************************************************************
         *   compose layout functions
         *
         ***************************************************************************************************/

        /***************************************************************************************************
         *   legacy layout functions
         *
         ***************************************************************************************************/
        fun makeTemporaryAlarm(workMode: Int = AlarmConstants.WORK_MODE_DIARY_WRITING): Alarm =
            if (application.config.enableJetpackRoomDatabase) {
                val alarms =
                    runBlocking { alarmRepository.getAllAlarms() }.sortedBy { it.alarmId }
                val maxId = alarms.maxOfOrNull { it.alarmId } ?: 0
                val count = alarms.size
                val sequence =
                    if (maxId == count) {
                        maxId + 1
                    } else {
                        var targetId = maxId + 1
                        for ((index, item) in alarms.withIndex()) {
                            val validSequence = index + 1
                            if (item.alarmId != validSequence) {
                                targetId = validSequence
                                break
                            }
                        }
                        targetId
                    }
                Alarm(alarmId = sequence, workMode = workMode)
            } else {
                EasyDiaryDbHelper.makeTemporaryAlarm(workMode)
            }

        suspend fun findAllAlarms(): List<Alarm> =
            alarmRepository
                .getAllAlarms()

        suspend fun findAlarmById(id: Int) =
            alarmRepository.getAlarmById(
                id,
            )

        suspend fun deleteAlarmById(id: Int) = alarmRepository.deleteAlarmById(id)
        /***************************************************************************************************
         *   common functions
         *
         ***************************************************************************************************/
    }
