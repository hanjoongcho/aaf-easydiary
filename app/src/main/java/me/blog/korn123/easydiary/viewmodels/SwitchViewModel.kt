package me.blog.korn123.easydiary.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SwitchViewModel : ViewModel() {
    var isOn: MutableLiveData<Boolean> = MutableLiveData()

    fun toggle() {
        isOn.value = isOn.value?.not()
    }
}