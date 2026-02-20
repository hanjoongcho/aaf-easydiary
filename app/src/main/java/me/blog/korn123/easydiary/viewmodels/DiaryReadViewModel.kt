package me.blog.korn123.easydiary.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.blog.korn123.easydiary.extensions.config

class DiaryReadViewModel(
    application: Application,
) : AndroidViewModel(application) {
    val config = application.config
    private val _isShowContentsCounting: MutableStateFlow<Boolean> = MutableStateFlow(config.enableCountCharacters)
    val isShowContentsCounting: StateFlow<Boolean> = _isShowContentsCounting.asStateFlow()

    private val _isShowAddress: MutableStateFlow<Boolean> = MutableStateFlow(config.enableLocationInfo)
    val isShowAddress: StateFlow<Boolean> = _isShowAddress.asStateFlow()
}
