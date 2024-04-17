package me.blog.korn123.easydiary.activities

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.helper.TransitionHelper
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Typeface
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ActivityQuickSettingsBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.executeScheduledTask
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
            setHomeAsUpIndicator(R.drawable.ic_cross)
        }

        mBinding.run {
            composeView.setContent {
                MaterialTheme {
                    PlantDetailDescription(context = this@QuickSettingsActivity)
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
                R.id.disable_future_diary -> {
                    disableFutureDiarySwitcher.toggle()
                    config.disableFutureDiary = disableFutureDiarySwitcher.isChecked
                }
                R.id.syncGoogleCalendar -> {
                    val alarm = Alarm().apply {
                        sequence = Int.MAX_VALUE
                        workMode = Alarm.WORK_MODE_CALENDAR_SCHEDULE_SYNC
                        label = "Quick Settings"
                    }
                    AlarmWorkExecutor(this@QuickSettingsActivity).run { executeWork(alarm) }
                }
            }
        }
        updateCardAlpha()
    }

    private fun bindEvent() {
        mBinding.run {
            enablePhotoHighlight.setOnClickListener(mOnClickListener)
            disableFutureDiary.setOnClickListener(mOnClickListener)
            syncGoogleCalendar.setOnClickListener(mOnClickListener)
        }
    }

    private fun initPreference() {
        mBinding.run {
            enablePhotoHighlightSwitcher.isChecked = config.enablePhotoHighlight
            disableFutureDiarySwitcher.isChecked = config.disableFutureDiary
            updateCardAlpha()
        }
    }

    private fun updateCardAlpha() {
        mBinding.run {
            enablePhotoHighlight.alpha = if (enablePhotoHighlightSwitcher.isChecked) 1.0f else 0.5f
            disableFutureDiary.alpha = if (disableFutureDiarySwitcher.isChecked) 1.0f else 0.5f
        }
    }

    @Composable
    fun PlantDetailDescription(context: Context) {
        val pixelValue = config.settingFontSize
        val density = LocalDensity.current
        val sp = with (density) {
            val temp = pixelValue.toDp()
            temp.toSp()
        }

        Card(
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(Color(context.config.backgroundColor)),
            modifier = Modifier.padding(3.dp)
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text(
                    text = "Sync Google Calendar",
                    style = TextStyle(
                        fontFamily = FontUtils.getComposeFontFamily(context),
                        fontWeight = FontWeight.Bold,
//                        fontStyle = FontStyle.Italic,
                        color = Color(context.config.textColor),
                        fontSize = TextUnit(sp.value, TextUnitType.Sp),
                    )
                )
            }
        }
    }

//    @Preview
//    @Composable
//    private fun PlantDetailDescriptionPreview() {
//        MaterialTheme {
//            PlantDetailDescription(LocalContext.current)
//        }
//    }
}