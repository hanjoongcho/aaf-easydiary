package me.blog.korn123.easydiary.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DiaryReadViewModel : ViewModel()  {
    val isShowContentsCounting: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    val isShowAddress: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
}