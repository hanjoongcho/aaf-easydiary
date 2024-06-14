package me.blog.korn123.easydiary.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.blog.korn123.easydiary.helper.SYMBOL_SELECT_ALL

class DiaryMainViewModel : ViewModel()  {
    val symbol: MutableLiveData<Int> = MutableLiveData(SYMBOL_SELECT_ALL)
    val isReady: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)

    fun updateSymbolSequence(symbolSequence: Int) {
        symbol.value = symbolSequence
    }
}