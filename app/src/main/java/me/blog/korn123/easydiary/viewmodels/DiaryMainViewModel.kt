package me.blog.korn123.easydiary.viewmodels

import android.os.SystemClock
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.blog.korn123.easydiary.helper.SYMBOL_SELECT_ALL

class DiaryMainViewModel : ViewModel()  {
    private val initTime = SystemClock.uptimeMillis()
    val symbol: MutableLiveData<Int> = MutableLiveData(SYMBOL_SELECT_ALL)

    fun updateSymbolSequence(symbolSequence: Int) {
        symbol.value = symbolSequence
    }
    fun isDataReady() = SystemClock.uptimeMillis() - initTime > WORK_DURATION

    companion object {
        const val WORK_DURATION = 1000L
    }
}