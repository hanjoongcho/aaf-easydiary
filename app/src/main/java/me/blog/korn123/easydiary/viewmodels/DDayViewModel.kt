package me.blog.korn123.easydiary.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import me.blog.korn123.easydiary.domain.model.DDay
import me.blog.korn123.easydiary.domain.repository.DDayRepository
import javax.inject.Inject

@HiltViewModel
class DDayViewModel
    @Inject
    constructor(
        application: Application,
        private val dDayRepository: DDayRepository,
    ) : AndroidViewModel(application) {
        /***************************************************************************************************
         *   compose layout functions
         *
         ***************************************************************************************************/

        /***************************************************************************************************
         *   legacy layout functions
         *
         ***************************************************************************************************/
        fun duplicateDDay(dDay: DDay): DDay = dDay.copy()

        /***************************************************************************************************
         *   common functions
         *
         ***************************************************************************************************/
    }
