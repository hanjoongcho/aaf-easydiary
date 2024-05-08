package me.blog.korn123.easydiary.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ActivityQuickSettingsBinding
import me.blog.korn123.easydiary.extensions.config
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

            Row {
                repeat(2) {
                    SimpleCard(
                        context,
                        currentTextUnit,
                        isPreview,
                        stringResource(id = R.string.sync_google_calendar_event_title),
                        stringResource(id = R.string.sync_google_calendar_event_summary),
                        Modifier
                            .fillMaxWidth()
//                            .wrapContentWidth()
                            .weight(1f)
                    ) {
                        val alarm = Alarm().apply {
                            sequence = Int.MAX_VALUE
                            workMode = Alarm.WORK_MODE_CALENDAR_SCHEDULE_SYNC
                            label = "Quick Settings"
                        }
                        AlarmWorkExecutor(this@QuickSettingsActivity).run { executeWork(alarm) }
                    }
                }
            }

            FlowRow(
                modifier = Modifier,
//                    .padding(3.dp, 3.dp)
//                    .fillMaxWidth(1f)
//                    .fillMaxHeight(1f),
//                horizontalArrangement = Arrangement.spacedBy(3.dp),
//                verticalArrangement = Arrangement.spacedBy(3.dp),
//                overflow = FlowRowOverflow.Clip,
            maxItemsInEachRow = 2
            ) {

                val itemModifier = Modifier
                    .padding(4.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(context.config.backgroundColor))

                val spaceModifier = Modifier
                repeat(11) { item ->
                    if ((item + 1) % 3 == 0) {
                        Spacer(modifier = itemModifier.fillMaxWidth())
                    } else {
                        Spacer(modifier = itemModifier.weight(0.5f))
                    }
                }
            }

            SimpleCard(
                context,
                currentTextUnit,
                isPreview,
                stringResource(id = R.string.sync_google_calendar_event_title),
                stringResource(id = R.string.sync_google_calendar_event_summary),
                Modifier
                    .fillMaxWidth()
                    .wrapContentWidth()
//                        .weight(1f)
            ) {
                val alarm = Alarm().apply {
                    sequence = Int.MAX_VALUE
                    workMode = Alarm.WORK_MODE_CALENDAR_SCHEDULE_SYNC
                    label = "Quick Settings"
                }
                AlarmWorkExecutor(this@QuickSettingsActivity).run { executeWork(alarm) }
            }
        }
    }


    @Preview(heightDp = 2000)
    @Composable
    private fun QuickSettingsPreview() {
        AppTheme(context = LocalContext.current) {
            QuickSettings(LocalContext.current, true)
        }
    }
}