package me.blog.korn123.easydiary.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.lifecycle.MutableLiveData
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
            setHomeAsUpIndicator(R.drawable.ic_cross)
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
                R.id.disable_future_diary -> {
                    disableFutureDiarySwitcher.toggle()
                    config.disableFutureDiary = disableFutureDiarySwitcher.isChecked
                }
            }
        }
        updateCardAlpha()
    }

    private fun bindEvent() {
        mBinding.run {
            enablePhotoHighlight.setOnClickListener(mOnClickListener)
            disableFutureDiary.setOnClickListener(mOnClickListener)
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
    fun QuickSettings(context: Context, isPreview: Boolean = false) {
        val pixelValue = context.config.settingFontSize
        val density = LocalDensity.current
        val sp = with (density) {
            val temp = pixelValue.toDp()
            temp.toSp()
        }
        var enablePhotoHighlight by remember { mutableStateOf(context.config.enablePhotoHighlight) }

        Row {
            Card(
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(Color(context.config.backgroundColor)),
                modifier = Modifier
                    .padding(3.dp),
                elevation = CardDefaults.cardElevation( defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sync",
                        style = TextStyle(
                            fontFamily = if (isPreview) null else FontUtils.getComposeFontFamily(context),
                            fontWeight = FontWeight.Bold,
//                        fontStyle = FontStyle.Italic,
                            color = Color(context.config.textColor),
                            fontSize = TextUnit(sp.value, TextUnitType.Sp),
                        ),
                    )
                    Switch(colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Red, // Change the color of the thumb when checked
                        checkedTrackColor = Color.Green, // Change the color of the track when checked
                        uncheckedThumbColor = Color.Blue, // Change the color of the thumb when unchecked
                        uncheckedTrackColor = Color.Gray // Change the color of the track when unchecked
                    ), modifier = Modifier.padding(start = 20.dp), checked = enablePhotoHighlight, onCheckedChange = { isChecked ->
                        context.config.enablePhotoHighlight = isChecked
                        enablePhotoHighlight = isChecked
                        initPreference()
                    })
                }
            }
            Card(
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(Color(context.config.backgroundColor)),
                modifier = Modifier
                    .padding(3.dp)
//                    .fillMaxWidth()

                    .clickable {
                        val alarm = Alarm().apply {
                            sequence = Int.MAX_VALUE
                            workMode = Alarm.WORK_MODE_CALENDAR_SCHEDULE_SYNC
                            label = "Quick Settings"
                        }
                        AlarmWorkExecutor(this@QuickSettingsActivity).run { executeWork(alarm) }
                    }
                ,
                elevation = CardDefaults.cardElevation( defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(15.dp)
                ) {
                    Text(
                        text = "Sync Google Calendar",
                        style = TextStyle(
                            fontFamily = if (isPreview) null else FontUtils.getComposeFontFamily(context),
                            fontWeight = FontWeight.Bold,
//                        fontStyle = FontStyle.Italic,
                            color = Color(context.config.textColor),
                            fontSize = TextUnit(sp.value, TextUnitType.Sp),
                        ),
                    )
                }
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