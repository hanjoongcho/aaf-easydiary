package me.blog.korn123.easydiary.viewmodels

import android.app.Application
import android.net.Uri
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.BuildConfig
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.DEFAULT_CALENDAR_FONT_SCALE

class SettingsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    val config = application.config

    /***************************************************************************************************
     *   Switch
     *
     ***************************************************************************************************/
    private val _enableReviewFlowVisible: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val enableReviewFlowVisible: StateFlow<Boolean> = _enableReviewFlowVisible.asStateFlow()

    fun setEnableReviewFlowVisible(isOn: Boolean) {
        _enableReviewFlowVisible.value = isOn
    }

    private val _enableCardViewPolicy: MutableStateFlow<Boolean> = MutableStateFlow(config.enableCardViewPolicy)
    val enableCardViewPolicy: StateFlow<Boolean> = _enableCardViewPolicy.asStateFlow()

    fun setEnableCardViewPolicy(isOn: Boolean) {
        _enableCardViewPolicy.value = isOn
    }

    private val _enableLocationInfo: MutableStateFlow<Boolean> = MutableStateFlow(config.enableLocationInfo)
    val enableLocationInfo: StateFlow<Boolean> get() = _enableLocationInfo.asStateFlow()

    fun setEnableLocationInfo(isOn: Boolean) {
        _enableLocationInfo.value = isOn
    }

    private val _enableShakeDetector: MutableStateFlow<Boolean> = MutableStateFlow(config.enableShakeDetector)
    val enableShakeDetector: StateFlow<Boolean> get() = _enableShakeDetector.asStateFlow()

    fun setEnableShakeDetector(isOn: Boolean) {
        _enableShakeDetector.value = isOn
    }

    /***************************************************************************************************
     *   SubDescription
     *
     ***************************************************************************************************/
    private val _thumbnailSizeSubDescription: MutableStateFlow<String> = MutableStateFlow("${config.settingThumbnailSize}dp x ${config.settingThumbnailSize}dp")
    val thumbnailSizeSubDescription: StateFlow<String> = _thumbnailSizeSubDescription.asStateFlow()

    private val _datetimeFormatSubDescription: MutableStateFlow<String> =
        MutableStateFlow(
            DateUtils.getDateTimeStringForceFormatting(
                System.currentTimeMillis(),
                application,
            ),
        )
    val datetimeFormatSubDescription: StateFlow<String> = _datetimeFormatSubDescription.asStateFlow()

    private val _summaryMaxLinesSubDescription: MutableStateFlow<String> = MutableStateFlow(application.getString(R.string.max_lines_value, config.summaryMaxLines))
    val summaryMaxLinesSubDescription: StateFlow<String> get() = _summaryMaxLinesSubDescription.asStateFlow()

    private val _fontSettingDescription: MutableStateFlow<String> = MutableStateFlow(FontUtils.fontFileNameToDisplayName(application, config.settingFontName))
    val fontSettingDescription: StateFlow<String> get() = _fontSettingDescription.asStateFlow()

    private val _calendarFontScaleDescription: MutableStateFlow<String> =
        MutableStateFlow(
            when (config.settingCalendarFontScale) {
                DEFAULT_CALENDAR_FONT_SCALE -> {
                    application.getString(R.string.calendar_font_scale_disable)
                }

                else -> {
                    application.getString(
                        R.string.calendar_font_scale_factor,
                        config.settingCalendarFontScale,
                    )
                }
            },
        )
    val calendarFontScaleDescription: StateFlow<String> get() = _calendarFontScaleDescription.asStateFlow()

    /***************************************************************************************************
     *   Setting Value
     *
     ***************************************************************************************************/
    private val _lineSpacingScaleFactor: MutableStateFlow<Float> = MutableStateFlow(config.lineSpacingScaleFactor)
    val lineSpacingScaleFactor: StateFlow<Float> = _lineSpacingScaleFactor.asStateFlow()

    fun setLineSpacingScaleFactor(value: Float) {
        _lineSpacingScaleFactor.value = value
    }

    private val _fontSize: MutableStateFlow<Float> = MutableStateFlow(config.settingFontSize)
    val fontSize: StateFlow<Float> = _fontSize.asStateFlow()

    fun setFontSize(value: Float) {
        _fontSize.value = value
    }

    private val _fontFamily: MutableStateFlow<FontFamily?> = MutableStateFlow(FontUtils.getComposeFontFamily(application))
    val fontFamily: StateFlow<FontFamily?> = _fontFamily.asStateFlow()

    fun setFontFamily(value: FontFamily?) {
        _fontFamily.value = value
    }

    /***************************************************************************************************
     *   GMS Backup
     *
     ***************************************************************************************************/
    private val _informationTitle: MutableStateFlow<String> = MutableStateFlow(application.getString(R.string.google_drive_account_sign_in_title))
    val informationTitle: StateFlow<String> get() = _informationTitle.asStateFlow()

    fun setInformationTitle(informationTitle: String) {
        _informationTitle.value = informationTitle
    }

    private val _accountInfo: MutableStateFlow<String> = MutableStateFlow(application.getString(R.string.google_drive_account_sign_in_description))
    val accountInfo: StateFlow<String> get() = _accountInfo.asStateFlow()

    fun setAccountInfo(accountInfo: String) {
        _accountInfo.value = accountInfo
    }

    private val _profileImageUrl: MutableStateFlow<Uri?> = MutableStateFlow(null)
    val profileImageUrl: StateFlow<Uri?> get() = _profileImageUrl.asStateFlow()

    fun setProfileImageUrl(profileImageUrl: Uri?) {
        _profileImageUrl.value = profileImageUrl
    }

    /***************************************************************************************************
     *   App Info
     *
     ***************************************************************************************************/
    private val _rateAppSettingSummary: MutableStateFlow<String> =
        MutableStateFlow(String.format("v%s_%s_%s (%d)", BuildConfig.VERSION_NAME, BuildConfig.FLAVOR, BuildConfig.BUILD_TYPE, BuildConfig.VERSION_CODE))
    val rateAppSettingSummary: StateFlow<String> = _rateAppSettingSummary.asStateFlow()

    private val _inviteSummary: MutableStateFlow<String> = MutableStateFlow("")
    val inviteSummary: StateFlow<String> get() = _inviteSummary.asStateFlow()

    fun setInviteSummary(inviteSummary: String) {
        _inviteSummary.value = inviteSummary
    }
}
