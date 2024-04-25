package me.blog.korn123.easydiary.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ActivityQuickSettingsBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.AlarmWorkExecutor
import me.blog.korn123.easydiary.models.Alarm

class QuickSettingsActivity : EasyDiaryActivity() {

    private lateinit var mBinding: ActivityQuickSettingsBinding
    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityQuickSettingsBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setSupportActionBar(mBinding.toolbar)
        supportActionBar?.run {
            setTitle("Quick Settings")
            setHomeAsUpIndicator(R.drawable.ic_cross)
            setDisplayHomeAsUpEnabled(true)
        }

        mBinding.run {
            composeView.setContent {
                MaterialTheme {
                    QuickSettings(context = this@QuickSettingsActivity)
                }
            }
        }

        bindEvent()
        initPreference()
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private val mOnClickListener = View.OnClickListener { view ->
        mBinding.run {
            when (view.id) {
                R.id.enable_photo_highlight -> {
                    enablePhotoHighlightSwitcher.toggle()
                    config.enablePhotoHighlight = enablePhotoHighlightSwitcher.isChecked
                }
            }
        }
        updateCardAlpha()
    }

    private fun bindEvent() {
        mBinding.run {
            enablePhotoHighlight.setOnClickListener(mOnClickListener)
        }
    }

    private fun initPreference() {
        mBinding.run {
            enablePhotoHighlightSwitcher.isChecked = config.enablePhotoHighlight
            updateCardAlpha()
        }
    }

    private fun updateCardAlpha() {
        mBinding.run {
            enablePhotoHighlight.alpha = if (enablePhotoHighlightSwitcher.isChecked) 1.0f else 0.5f
        }
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    fun QuickSettings(context: Context, isPreview: Boolean = false) {
        val pixelValue = context.config.settingFontSize
        val density = LocalDensity.current
        val currentTextUnit = with (density) {
            val temp = pixelValue.toDp()
            temp.toSp()
        }
        var enablePhotoHighlight by remember { mutableStateOf(context.config.enablePhotoHighlight) }
        var disableFutureDiary by remember { mutableStateOf(context.config.disableFutureDiary) }

        FlowRow {
            SwitchCard(
                context,
                currentTextUnit,
                isPreview,
                "첨부사진 하이라이트",
                enablePhotoHighlight
            ) {
                context.config.enablePhotoHighlight = !enablePhotoHighlight
                enablePhotoHighlight = !enablePhotoHighlight
                initPreference()
            }
            SwitchCard(
                context,
                currentTextUnit,
                isPreview,
                "미래일정 숨김",
                disableFutureDiary
            ) {
                context.config.disableFutureDiary = !disableFutureDiary
                disableFutureDiary = !disableFutureDiary
            }
            SimpleCard(context, currentTextUnit, isPreview, "Sync Google Calendar") {
                val alarm = Alarm().apply {
                    sequence = Int.MAX_VALUE
                    workMode = Alarm.WORK_MODE_CALENDAR_SCHEDULE_SYNC
                    label = "Quick Settings"
                }
                AlarmWorkExecutor(this@QuickSettingsActivity).run { executeWork(alarm) }
            }
            SimpleCard(context, currentTextUnit, isPreview, "Setting-A") {}
        }
    }

    @Composable
    private fun SimpleCard(
        context: Context,
        fontSize: TextUnit,
        isPreview: Boolean = false,
        title: String,
        callback: () -> Unit
    ) {
        Card(
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(Color(context.config.backgroundColor)),
            modifier = Modifier
                .padding(3.dp)
//                    .fillMaxWidth()
                .clickable {
                    callback.invoke()
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = if (isPreview) null else FontUtils.getComposeFontFamily(context),
                        fontWeight = FontWeight.Bold,
//                        fontStyle = FontStyle.Italic,
                        color = Color(context.config.textColor),
                        fontSize = TextUnit(fontSize.value, TextUnitType.Sp),
                    ),
                )
            }
        }
    }

    @Composable
    private fun SwitchCard(
        context: Context,
        textUnit: TextUnit,
        isPreview: Boolean = false,
        title: String,
        isOn: Boolean,
        callback: () -> Unit
    ) {
        Card(
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(Color(context.config.backgroundColor)),
            modifier = Modifier.padding(3.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            onClick = {
                callback.invoke()
            }
        ) {
            Row(
                modifier = Modifier.padding(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontFamily = if (isPreview) null else FontUtils.getComposeFontFamily(context),
                        fontWeight = FontWeight.Bold,
//                        fontStyle = FontStyle.Italic,
                        color = Color(context.config.textColor),
                        fontSize = TextUnit(textUnit.value, TextUnitType.Sp),
                    ),
                )
                Switch(
//                        modifier = Modifier.scale(0.8F),
                    modifier = Modifier.padding(start = 10.dp),
                    checked = isOn,
                    colors = SwitchDefaults.colors(
//                            checkedThumbColor = Color(context.config.primaryColor),
//                            checkedTrackColor = Color(ColorUtils.setAlphaComponent(context.config.primaryColor, 150)),
                    ),
                    onCheckedChange = {
                        callback.invoke()
                    },
                    thumbContent = if (isOn) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    } else {
                        null
                    }
                )
            }
        }
    }

    @Preview
    @Composable
    private fun QuickSettingsPreview() {
        MaterialTheme {
            QuickSettings(LocalContext.current, true)
        }
    }
}