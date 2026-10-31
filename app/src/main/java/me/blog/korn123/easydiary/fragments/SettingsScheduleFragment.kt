package me.blog.korn123.easydiary.fragments

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.moveLastItemToFront
import com.simplemobiletools.commons.extensions.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.BuildConfig
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.AlarmAdapter
import me.blog.korn123.easydiary.databinding.DialogAlarmBinding
import me.blog.korn123.easydiary.databinding.FragmentSettingsScheduleBinding
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.extensions.addBit
import me.blog.korn123.easydiary.extensions.alarmRepository
import me.blog.korn123.easydiary.extensions.cancelAlarmClock
import me.blog.korn123.easydiary.extensions.checkPermission
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.getFormattedTime
import me.blog.korn123.easydiary.extensions.getSelectedDaysString
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.isLandScape
import me.blog.korn123.easydiary.extensions.makeSnackBar
import me.blog.korn123.easydiary.extensions.removeBit
import me.blog.korn123.easydiary.extensions.scheduleNextAlarm
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.extensions.updateAlertDialog
import me.blog.korn123.easydiary.extensions.updateAppViews
import me.blog.korn123.easydiary.extensions.updateDrawableColorInnerCardView
import me.blog.korn123.easydiary.extensions.updateFragmentUI
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.helper.AAF_TEST
import me.blog.korn123.easydiary.helper.AlarmConstants
import me.blog.korn123.easydiary.helper.REQUEST_CODE_SCHEDULE_EXACT_ALARM
import me.blog.korn123.easydiary.presentation.alarm.AlarmViewModel
import me.blog.korn123.easydiary.utils.FontUtils
import kotlin.math.pow
import me.blog.korn123.easydiary.domain.model.Alarm as AlarmDomain

@AndroidEntryPoint
class SettingsScheduleFragment : Fragment() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var binding: FragmentSettingsScheduleBinding
    private lateinit var alarmAdapter: AlarmAdapter
    private var alarmList: ArrayList<AlarmDomain> = arrayListOf()
    private val alarmViewModel: AlarmViewModel by viewModels()
    private val currentActivity: Activity
        get() = requireActivity()

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSettingsScheduleBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        updateFragmentUI(binding.root)
        alarmAdapter =
            AlarmAdapter(
                currentActivity,
                alarmList,
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    openAlarmDialog(alarmList[position].copy(), alarmList[position])
                },
            )

        binding.alarmRecyclerView.apply {
            layoutManager = androidx.recyclerview.widget.GridLayoutManager(currentActivity, if (requireActivity().isLandScape()) 2 else 1)
            addItemDecoration(SpacesItemDecoration(0))
            adapter = alarmAdapter
        }

        initProperties()
        updateAlarmList()
    }

    override fun onResume() {
        super.onResume()
        updateFragmentUI(binding.root)
        currentActivity.updateDrawableColorInnerCardView(R.drawable.ic_delete_w)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        requireActivity().run {
            when (requestCode) {
                REQUEST_CODE_SCHEDULE_EXACT_ALARM -> {
                    if (checkPermission(arrayOf(Manifest.permission.SCHEDULE_EXACT_ALARM))) {
                        openAlarmDialog(temporaryAlarm)
                    } else {
                        makeSnackBar(
                            findViewById(android.R.id.content),
                            getString(R.string.guide_message_3),
                        )
                    }
                }

                else -> {}
            }
        }
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private lateinit var temporaryAlarm: AlarmDomain

    fun openAlarmDialog(
        targetAlarm: AlarmDomain,
        storedAlarm: AlarmDomain? = null,
    ) {
        temporaryAlarm = targetAlarm

        currentActivity.run activity@{
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            } else {
                val dialogAlarmBinding = DialogAlarmBinding.inflate(layoutInflater)
                var alertDialog: AlertDialog? = null
                val builder =
                    AlertDialog.Builder(this).apply {
                        setCancelable(false)
                        setPositiveButton(getString(android.R.string.ok), null)
                        setNegativeButton(getString(android.R.string.cancel)) { _, _ -> alertDialog?.dismiss() }
                    }
                dialogAlarmBinding.run {
                    val dayLetters = resources.getStringArray(R.array.week_day_letters).toMutableList()
                    val dayIndexes = arrayListOf(0, 1, 2, 3, 4, 5, 6)
                    if (config.isSundayFirst) {
                        dayIndexes.moveLastItemToFront()
                    }
                    linearAlarmDaysHolder.removeAllViews()
                    dayIndexes.forEach {
                        val pow = 2.0.pow(it.toDouble()).toInt()
                        val day = layoutInflater.inflate(R.layout.partial_alarm_day, linearAlarmDaysHolder, false) as TextView
                        day.text = dayLetters[it]

                        val isDayChecked = targetAlarm.days and pow != 0
                        day.background = getProperDayDrawable(isDayChecked)

                        day.setTextColor(if (isDayChecked) config.backgroundColor else config.textColor)
                        day.setOnClickListener {
                            val selectDay = targetAlarm.days and pow == 0
                            targetAlarm.days =
                                if (selectDay) {
                                    targetAlarm.days.addBit(pow)
                                } else {
                                    targetAlarm.days.removeBit(pow)
                                }
                            day.background = getProperDayDrawable(selectDay)
                            day.setTextColor(if (selectDay) config.backgroundColor else config.textColor)
                            textAlarmDays.text = getSelectedDaysString(targetAlarm.days)
                        }
                        linearAlarmDaysHolder.addView(day)
                    }
                    textAlarmDays.text = getSelectedDaysString(targetAlarm.days)
                    textAlarmDays.setTextColor(config.textColor)
                    switchAlarm.isChecked = targetAlarm.isEnabled
                    editAlarmDescription.setText(targetAlarm.label)
                    textAlarmTime.text = getFormattedTime(targetAlarm.timeInMinutes * 60, false, true)
                    textAlarmTime.setOnClickListener {
                        TimePickerDialog(
                            this@activity,
                            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                                targetAlarm.timeInMinutes = hourOfDay * 60 + minute
                                textAlarmTime.text = getFormattedTime(targetAlarm.timeInMinutes * 60, false, true)
                            },
                            targetAlarm.timeInMinutes / 60,
                            targetAlarm.timeInMinutes % 60,
                            DateFormat.is24HourFormat(this@activity),
                        ).show()
                    }
                    switchAlarm.setOnCheckedChangeListener { _, isChecked ->
                        targetAlarm.isEnabled = isChecked
                    }
                    when (storedAlarm == null) {
                        true -> {
                            imageDeleteAlarm.visibility = View.GONE
                        }

                        false -> {
                            imageDeleteAlarm.setOnClickListener {
                                showAlertDialog(
                                    "Are you sure you want to delete the selected schedule?",
                                    DialogInterface.OnClickListener { _, _ ->
                                        lifecycleScope.launch {
                                            cancelAlarmClock(targetAlarm)
                                            alertDialog?.dismiss()
                                            alarmRepository.deleteAlarmById(storedAlarm.alarmId)
                                            updateAlarmList()
                                        }
                                    },
                                    { _, _ -> },
                                    DialogMode.WARNING,
                                )
                            }
                        }
                    }
                    when (targetAlarm.workMode) {
                        AlarmConstants.WORK_MODE_DIARY_WRITING -> radioDiaryWriting.isChecked = true
                        AlarmConstants.WORK_MODE_DIARY_BACKUP_LOCAL -> radioDiaryBackupLocal.isChecked = true
                        AlarmConstants.WORK_MODE_DIARY_BACKUP_GMS -> radioDiaryBackupGms.isChecked = true
                        AlarmConstants.WORK_MODE_CALENDAR_SCHEDULE_SYNC -> radioCalendarScheduleSync.isChecked = true
                    }
                    radioGroupWorkMode.setOnCheckedChangeListener { _, i ->
                        when (i) {
                            R.id.radio_diary_writing -> targetAlarm.workMode = AlarmConstants.WORK_MODE_DIARY_WRITING
                            R.id.radio_diary_backup_local -> targetAlarm.workMode = AlarmConstants.WORK_MODE_DIARY_BACKUP_LOCAL
                            R.id.radio_diary_backup_gms -> targetAlarm.workMode = AlarmConstants.WORK_MODE_DIARY_BACKUP_GMS
                            R.id.radio_calendar_schedule_sync -> targetAlarm.workMode = AlarmConstants.WORK_MODE_CALENDAR_SCHEDULE_SYNC
                        }
                    }

                    root.setBackgroundColor(config.backgroundColor)
                    this@activity.initTextSize(root)
                    updateTextColors(root)
                    updateAppViews(root)
                    FontUtils.setFontsTypeface(this@activity, null, root)

                    if (BuildConfig.FLAVOR == "foss") radioDiaryBackupGms.visibility = View.GONE
                }

                alertDialog =
                    builder.create().apply {
                        updateAlertDialog(this, null, dialogAlarmBinding.root, getString(R.string.preferences_category_schedule))
                        getButton(AlertDialog.BUTTON_POSITIVE).run {
                            setOnClickListener {
                                when {
                                    dialogAlarmBinding.textAlarmDays.text.isEmpty() -> {
                                        toast("Please select days.")
                                    }

                                    dialogAlarmBinding.editAlarmDescription.text?.isEmpty() ?: true -> {
                                        dialogAlarmBinding.editAlarmDescription.run {
                                            requestFocus()
                                            toast("Please input schedule description.")
                                            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                                        }
                                    }

                                    else -> {
                                        // update alarm schedule
                                        if (targetAlarm.isEnabled) {
                                            scheduleNextAlarm(targetAlarm, true)
                                        } else {
                                            cancelAlarmClock(targetAlarm)
                                        }

                                        // save alarm
                                        lifecycleScope.launch {
                                            targetAlarm.label = dialogAlarmBinding.editAlarmDescription.text.toString()
                                            alarmRepository.updateAlarm(targetAlarm)
                                            alertDialog?.dismiss()
                                            updateAlarmList()
                                            alertDialog?.dismiss()
                                        }
                                    }
                                }
                            }
                        }
                    }
            }
        }
    }

    private fun updateAlarmList() {
        Log.i(AAF_TEST, "updateAlarmList")
        lifecycleScope.launch {
            alarmList.run {
                clear()
                addAll(alarmViewModel.findAllAlarms())
                binding.infoMessage.visibility = if (this.isEmpty()) View.VISIBLE else View.GONE
                Log.i(AAF_TEST, this.toString())
            }
            alarmAdapter.notifyDataSetChanged()
        }
    }

    private fun getProperDayDrawable(selected: Boolean): Drawable {
        val drawableId = if (selected) R.drawable.bg_circle_filled else R.drawable.bg_circle_stroke
        val drawable = ContextCompat.getDrawable(currentActivity, drawableId)
        drawable?.applyColorFilter(currentActivity.config.textColor)
        return requireNotNull(drawable)
    }

    private fun initProperties() {
        currentActivity.config.use24HourFormat = false
    }

    class SpacesItemDecoration(
        private val space: Int,
    ) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: androidx.recyclerview.widget.RecyclerView,
            state: androidx.recyclerview.widget.RecyclerView.State,
        ) {
            val position = parent.getChildAdapterPosition(view)
            when (position == 0) {
                true -> outRect.top = 0
                false -> outRect.top = space
            }
        }
    }
}
