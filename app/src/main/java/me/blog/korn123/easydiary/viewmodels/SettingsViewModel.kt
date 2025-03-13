package me.blog.korn123.easydiary.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {
    var enableCardViewPolicy: MutableLiveData<Boolean> = MutableLiveData()
}