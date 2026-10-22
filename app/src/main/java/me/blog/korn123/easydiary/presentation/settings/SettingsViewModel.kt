package me.blog.korn123.easydiary.presentation.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.blog.korn123.easydiary.extensions.config

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
}
