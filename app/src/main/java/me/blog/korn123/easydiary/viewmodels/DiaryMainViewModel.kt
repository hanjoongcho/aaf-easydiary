package me.blog.korn123.easydiary.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DiaryMainViewModel : ViewModel()  {
    val symbol: MutableLiveData<Int> = MutableLiveData(9999)

    fun updateSymbolSequence(symbolSequence: Int) {
        symbol.value = symbolSequence
    }
}