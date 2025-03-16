package me.blog.korn123.easydiary.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {

    /***************************************************************************************************
     *   Switch
     *
     ***************************************************************************************************/
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

    private val _enableShakeDetector: MutableLiveData<Boolean> = MutableLiveData()
    val enableShakeDetector: LiveData<Boolean> get() = _enableShakeDetector
    fun enableShakeDetectorIsOn() = _enableShakeDetector.value == true
    fun setEnableShakeDetector(isOn: Boolean) { _enableShakeDetector.value = isOn }


    /***************************************************************************************************
     *   SubDescription
     *
     ***************************************************************************************************/
    private val _settingThumbnailSizeSubDescription: MutableLiveData<String> = MutableLiveData()
    val thumbnailSizeSubDescription: LiveData<String> get() = _settingThumbnailSizeSubDescription
    fun setThumbnailSizeSubDescription(description: String) { _settingThumbnailSizeSubDescription.value = description }

    private val _settingDatetimeFormat: MutableLiveData<String> = MutableLiveData()
    val datetimeFormatSubDescription: LiveData<String> get() = _settingDatetimeFormat
    fun setDatetimeFormatSubDescription(description: String) { _settingDatetimeFormat.value = description }

    private val _summaryMaxLines: MutableLiveData<String> = MutableLiveData()
    val summaryMaxLinesSubDescription: LiveData<String> get() = _summaryMaxLines
    fun setSummaryMaxLinesSubDescription(description: String) { _summaryMaxLines.value = description }

    private val _fontSetting: MutableLiveData<String> = MutableLiveData()
    val fontSettingDescription: LiveData<String> get() = _fontSetting
    fun setFontSettingDescription(description: String) { _fontSetting.value = description }

    private val _settingCalendarFontScale: MutableLiveData<String> = MutableLiveData()
    val calendarFontScaleDescription: LiveData<String> get() = _settingCalendarFontScale
    fun setCalendarFontScaleDescription(description: String) { _settingCalendarFontScale.value = description }


    /***************************************************************************************************
     *   Setting Value
     *
     ***************************************************************************************************/
    private val _lineSpacingScaleFactor: MutableLiveData<Float> = MutableLiveData()
    val lineSpacingScaleFactor: LiveData<Float> get() = _lineSpacingScaleFactor
    fun setLineSpacingScaleFactor(value: Float) { _lineSpacingScaleFactor.value = value }

    private val _fontSize: MutableLiveData<Float> = MutableLiveData()
    val fontSize: LiveData<Float> get() = _fontSize
    fun setFontSize(value: Float) { _fontSize.value = value }


}