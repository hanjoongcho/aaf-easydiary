package me.blog.korn123.easydiary.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
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
import me.blog.korn123.easydiary.extensions.makeToast
import me.blog.korn123.easydiary.helper.AlarmWorkExecutor
import me.blog.korn123.easydiary.models.Alarm
import me.blog.korn123.easydiary.ui.components.SimpleCard
import me.blog.korn123.easydiary.ui.components.SwitchCard
import me.blog.korn123.easydiary.ui.theme.AppTheme

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
                AppTheme(context = LocalContext.current) {
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

        Column {
            SwitchCard(
                context,
                currentTextUnit,
                isPreview,
                stringResource(R.string.enable_photo_highlight_title),
                Modifier.fillMaxWidth(),
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
                Modifier.fillMaxWidth(),
                disableFutureDiary
            ) {
                context.config.disableFutureDiary = !disableFutureDiary
                disableFutureDiary = !disableFutureDiary
            }



            FlowRow(
                modifier = Modifier
//                    .padding(3.dp, 3.dp)
                    .fillMaxWidth(1f)
                    .fillMaxHeight(1f),
//                horizontalArrangement = Arrangement.spacedBy(3.dp),
//                verticalArrangement = Arrangement.spacedBy(3.dp),
//                overflow = FlowRowOverflow.Clip,
            maxItemsInEachRow = 2
            ) {
                SimpleCard(
                    context,
                    currentTextUnit,
                    isPreview,
                    stringResource(id = R.string.sync_google_calendar_event_title),
                    stringResource(id = R.string.sync_google_calendar_event_summary),
//                Modifier.wrapContentHeight()
                    Modifier
                ) {
                    val alarm = Alarm().apply {
                        sequence = Int.MAX_VALUE
                        workMode = Alarm.WORK_MODE_CALENDAR_SCHEDULE_SYNC
                        label = "Quick Settings"
                    }
                    AlarmWorkExecutor(this@QuickSettingsActivity).run { executeWork(alarm) }
                }

                repeat(1) {
                    SimpleCard(context, currentTextUnit, isPreview, "[${it}] Water Melon weight-1", "Description...", Modifier.weight(1f)) { context.makeToast("[${it}] Water Melon") }
                    SimpleCard(context, currentTextUnit, isPreview, "[${it}] Banana weight-1", "Description...", Modifier.weight(1f)) { context.makeToast("[${it}] Banana") }
                    SimpleCard(context, currentTextUnit, isPreview, "[${it}] TURKEYTURKEYTURKEY weight-1", "Description...", Modifier.weight(1f)) { context.makeToast("[${it}] Cocoa") }
                }
//            val itemModifier = Modifier
////                .padding(14.dp)
////                .height(60.dp)
////                .width(150.dp)
//                .weight(1f)
//                .clip(RoundedCornerShape(8.dp))
////                .background(Color.DarkGray)
//            repeat(5) {
//                Surface(
//                    modifier = itemModifier,
//                    color = Color(context.config.backgroundColor)
//                ) {
//                    Text(
//                        modifier = Modifier.padding(15.dp),
//                        text = "Surface Text",
//                        style = TextStyle(
//                            fontFamily = if (isPreview) null else FontUtils.getComposeFontFamily(
//                                context
//                            ),
//                            fontWeight = FontWeight.Bold,
//                            color = Color(context.config.textColor),
//                            fontSize = TextUnit(currentTextUnit.value, TextUnitType.Sp),
//                        )
//                    )
//                }
//            }
//            Surface(
//                modifier = Modifier
//                    .clip(RoundedCornerShape(8.dp))
//                    .padding(3.dp),
//                color = Color(context.config.backgroundColor)
//            ) {
//                Button(
//                    modifier = Modifier
//                        .padding(3.dp)
//                        .background(color = Color(context.config.backgroundColor)),
//                    contentPadding = PaddingValues(10.dp),
//                    colors = ButtonColors(
//                        Color(context.config.backgroundColor),
//                        Color(context.config.textColor),
//                        Color(context.config.backgroundColor),
//                        Color(context.config.textColor)
//                    ),
//                    onClick = {}) {
//                    Text(
//                        modifier = Modifier, text = "Button Text", style = TextStyle(
//                            fontFamily = if (isPreview) null else FontUtils.getComposeFontFamily(
//                                context
//                            ),
//                            fontWeight = FontWeight.Bold,
//                            color = Color(context.config.textColor),
//                            fontSize = TextUnit(currentTextUnit.value, TextUnitType.Sp),
//                        )
//                    )
//                }
//            }
            }
        }
    }


    @Preview
    @Composable
    private fun QuickSettingsPreview() {
        AppTheme(context = LocalContext.current) {
            QuickSettings(LocalContext.current, true)
        }
    }
}