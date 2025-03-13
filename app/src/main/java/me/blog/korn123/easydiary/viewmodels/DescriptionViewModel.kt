package me.blog.korn123.easydiary.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DescriptionViewModel : ViewModel() {
    var settingThumbnailSize: MutableLiveData<String> = MutableLiveData()
    var settingDatetimeFormat: MutableLiveData<String> = MutableLiveData()
    var summaryMaxLines: MutableLiveData<String> = MutableLiveData()
}