package me.blog.korn123.easydiary.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {
    private val _enableReviewFlowVisible: MutableLiveData<Boolean> = MutableLiveData(true)
    val enableReviewFlowVisible: LiveData<Boolean> get() = _enableReviewFlowVisible
    fun enableReviewFlowVisibleIsOn() = _enableReviewFlowVisible.value == true
    fun setEnableReviewFlowVisible(isOn: Boolean) { _enableReviewFlowVisible.value = isOn }

    private val _enableCardViewPolicy: MutableLiveData<Boolean> = MutableLiveData()
    val enableCardViewPolicy: LiveData<Boolean> get() = _enableCardViewPolicy
    fun enableCardViewPolicyIsOn() = _enableCardViewPolicy.value == true
    fun setEnableCardViewPolicy(isOn: Boolean) { _enableCardViewPolicy.value = isOn }

    private val _enableLocationInfo: MutableLiveData<Boolean> = MutableLiveData()
    val enableLocationInfo: LiveData<Boolean> get() = _enableLocationInfo
    fun enableLocationInfoIsOn() = _enableLocationInfo.value == true
    fun setEnableLocationInfo(isOn: Boolean) { _enableLocationInfo.value = isOn }

    private val _settingThumbnailSizeSubDescription: MutableLiveData<String> = MutableLiveData()
    val thumbnailSizeSubDescription: LiveData<String> get() = _settingThumbnailSizeSubDescription
    fun setThumbnailSizeSubDescription(description: String) { _settingThumbnailSizeSubDescription.value = description }

    private val _settingDatetimeFormat: MutableLiveData<String> = MutableLiveData()
    val datetimeFormatSubDescription: LiveData<String> get() = _settingDatetimeFormat
    fun setDatetimeFormatSubDescription(description: String) { _settingDatetimeFormat.value = description }

    private val _summaryMaxLines: MutableLiveData<String> = MutableLiveData()
    val summaryMaxLinesSubDescription: LiveData<String> get() = _summaryMaxLines
    fun setSummaryMaxLinesSubDescription(description: String) { _summaryMaxLines.value = description }
}