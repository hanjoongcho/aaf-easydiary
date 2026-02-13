package me.blog.korn123.easydiary.viewmodels

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontFamily
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

    fun setEnableReviewFlowVisible(isOn: Boolean) {
        _enableReviewFlowVisible.value = isOn
    }

    private val _enableCardViewPolicy: MutableLiveData<Boolean> = MutableLiveData()
    val enableCardViewPolicy: LiveData<Boolean> get() = _enableCardViewPolicy

    fun enableCardViewPolicyIsOn() = _enableCardViewPolicy.value == true

    fun setEnableCardViewPolicy(isOn: Boolean) {
        _enableCardViewPolicy.value = isOn
    }

    private val _enableLocationInfo: MutableLiveData<Boolean> = MutableLiveData()
    val enableLocationInfo: LiveData<Boolean> get() = _enableLocationInfo

    fun enableLocationInfoIsOn() = _enableLocationInfo.value == true

    fun setEnableLocationInfo(isOn: Boolean) {
        _enableLocationInfo.value = isOn
    }

    private val _enableShakeDetector: MutableLiveData<Boolean> = MutableLiveData()
    val enableShakeDetector: LiveData<Boolean> get() = _enableShakeDetector

    fun enableShakeDetectorIsOn() = _enableShakeDetector.value == true

    fun setEnableShakeDetector(isOn: Boolean) {
        _enableShakeDetector.value = isOn
    }

    /***************************************************************************************************
     *   SubDescription
     *
     ***************************************************************************************************/
    private val _settingThumbnailSizeSubDescription: MutableLiveData<String> = MutableLiveData()
    val thumbnailSizeSubDescription: LiveData<String> get() = _settingThumbnailSizeSubDescription

    fun setThumbnailSizeSubDescription(description: String) {
        _settingThumbnailSizeSubDescription.value = description
    }

    private val _settingDatetimeFormat: MutableLiveData<String> = MutableLiveData()
    val datetimeFormatSubDescription: LiveData<String> get() = _settingDatetimeFormat

    fun setDatetimeFormatSubDescription(description: String) {
        _settingDatetimeFormat.value = description
    }

    private val _summaryMaxLines: MutableLiveData<String> = MutableLiveData()
    val summaryMaxLinesSubDescription: LiveData<String> get() = _summaryMaxLines

    fun setSummaryMaxLinesSubDescription(description: String) {
        _summaryMaxLines.value = description
    }

    private val _fontSetting: MutableLiveData<String> = MutableLiveData()
    val fontSettingDescription: LiveData<String> get() = _fontSetting

    fun setFontSettingDescription(description: String) {
        _fontSetting.value = description
    }

    private val _settingCalendarFontScale: MutableLiveData<String> = MutableLiveData()
    val calendarFontScaleDescription: LiveData<String> get() = _settingCalendarFontScale

    fun setCalendarFontScaleDescription(description: String) {
        _settingCalendarFontScale.value = description
    }

    /***************************************************************************************************
     *   Setting Value
     *
     ***************************************************************************************************/
    private val _lineSpacingScaleFactor: MutableLiveData<Float> = MutableLiveData()
    val lineSpacingScaleFactor: LiveData<Float> get() = _lineSpacingScaleFactor

    fun setLineSpacingScaleFactor(value: Float) {
        _lineSpacingScaleFactor.value = value
    }

    private val _fontSize: MutableLiveData<Float> = MutableLiveData()
    val fontSize: LiveData<Float> get() = _fontSize

    fun setFontSize(value: Float) {
        _fontSize.value = value
    }

    private val _fontFamily: MutableLiveData<FontFamily?> = MutableLiveData<FontFamily?>()
    val fontFamily: LiveData<FontFamily?> get() = _fontFamily

    fun setFontFamily(value: FontFamily?) {
        _fontFamily.value = value
    }

    var profilePicUri by mutableStateOf<String?>(null)

    /***************************************************************************************************
     *   GMS Backup
     *
     ***************************************************************************************************/
    private val _informationTitle: MutableLiveData<String> = MutableLiveData()
    val informationTitle: LiveData<String> get() = _informationTitle

    fun setInformationTitle(informationTitle: String) {
        _informationTitle.value = informationTitle
    }

    private val _accountInfo: MutableLiveData<String> = MutableLiveData()
    val accountInfo: LiveData<String> get() = _accountInfo

    fun setAccountInfo(accountInfo: String) {
        _accountInfo.value = accountInfo
    }

    private val _profileImageUrl: MutableLiveData<Uri?> = MutableLiveData()
    val profileImageUrl: LiveData<Uri?> get() = _profileImageUrl

    fun setProfileImageUrl(profileImageUrl: Uri?) {
        _profileImageUrl.value = profileImageUrl
    }

    /***************************************************************************************************
     *   App Info
     *
     ***************************************************************************************************/
    private val _rateAppSettingSummary: MutableLiveData<String> = MutableLiveData()
    val rateAppSettingSummary: LiveData<String> get() = _rateAppSettingSummary

    fun setRateAppSettingSummary(rateAppSettingSummary: String) {
        _rateAppSettingSummary.value = rateAppSettingSummary
    }

    private val _inviteSummary: MutableLiveData<String> = MutableLiveData()
    val inviteSummary: LiveData<String> get() = _inviteSummary

    fun setInviteSummary(inviteSummary: String) {
        _inviteSummary.value = inviteSummary
    }
}
