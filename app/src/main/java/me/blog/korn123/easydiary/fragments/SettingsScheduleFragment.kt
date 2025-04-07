package me.blog.korn123.easydiary.fragments

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.getSelectedDaysString
import com.simplemobiletools.commons.extensions.moveLastItemToFront
import com.simplemobiletools.commons.extensions.toast
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.BuildConfig
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.AlarmAdapter
import me.blog.korn123.easydiary.databinding.DialogAlarmBinding
import me.blog.korn123.easydiary.databinding.FragmentSettingsScheduleBinding
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.extensions.addBit
import me.blog.korn123.easydiary.extensions.cancelAlarmClock
import me.blog.korn123.easydiary.extensions.changeDrawableIconColor
import me.blog.korn123.easydiary.extensions.checkPermission
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.getFormattedTime
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
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.REQUEST_CODE_SCHEDULE_EXACT_ALARM
import me.blog.korn123.easydiary.models.Alarm
import kotlin.math.pow


class SettingsScheduleFragment() : androidx.fragment.app.Fragment() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: FragmentSettingsScheduleBinding
    private lateinit var mAlarmAdapter: AlarmAdapter
    private var mAlarmList: ArrayList<Alarm> = arrayListOf()
    private val mActivity: Activity
        get() = requireActivity()
    private val mRequestPermissionScheduleExactAlarmLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openAlarmDialog(mTemporaryAlarm)
        }
    }


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = FragmentSettingsScheduleBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateFragmentUI(mBinding.root)
        mAlarmAdapter = AlarmAdapter(
                mActivity,
                mAlarmList,
                AdapterView.OnItemClickListener { _, _, position, _ ->
                    openAlarmDialog(EasyDiaryDbHelper.duplicateAlarmBy(mAlarmList[position]), mAlarmList[position])
                }
        )

        mBinding.alarmRecyclerView.apply {
            layoutManager = androidx.recyclerview.widget.GridLayoutManager(mActivity, if (requireActivity().isLandScape()) 2 else 1)
//            addItemDecoration(SpacesItemDecoration(resources.getDimensionPixelSize(R.dimen.component_margin_small)))
            addItemDecoration(SpacesItemDecoration(0))
            adapter = mAlarmAdapter
        }

        initProperties()
        updateAlarmList()
    }

    override fun onResume() {
        super.onResume()
        updateFragmentUI(mBinding.root)
        mActivity.updateDrawableColorInnerCardView(R.drawable.ic_delete_w)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        requireActivity().run {
            when (requestCode) {
                REQUEST_CODE_SCHEDULE_EXACT_ALARM -> if (checkPermission(arrayOf(Manifest.permission.SCHEDULE_EXACT_ALARM))) {
                    openAlarmDialog(mTemporaryAlarm)
                } else {
                    makeSnackBar(
                        findViewById(android.R.id.content),
                        getString(R.string.guide_message_3)
                    )
                }
                else -> {}
            }
        }
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private lateinit var mTemporaryAlarm: Alarm
    fun openAlarmDialog(temporaryAlarm: Alarm, storedAlarm: Alarm? = null) {
        mTemporaryAlarm = temporaryAlarm

        mActivity.run activity@ {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            } else {
                val dialogAlarmBinding = DialogAlarmBinding.inflate(layoutInflater)
                var alertDialog: AlertDialog? = null
                val builder = AlertDialog.Builder(this).apply {
                    setCancelable(false)
                    setPositiveButton(getString(android.R.string.ok), null)
                    setNegativeButton(getString(android.R.string.cancel)) { _, _ -> alertDialog?.dismiss() }
                }
                dialogAlarmBinding.run {
                    val dayLetters = resources.getStringArray(R.array.week_day_letters).toList() as ArrayList<String>
                    val dayIndexes = arrayListOf(0, 1, 2, 3, 4, 5, 6)
                    if (config.isSundayFirst) {
                        dayIndexes.moveLastItemToFront()
                    }
                    linearAlarmDaysHolder.removeAllViews()
                    dayIndexes.forEach {
                        val pow = 2.0.pow(it.toDouble()).toInt()
                        val day = layoutInflater.inflate(R.layout.partial_alarm_day, linearAlarmDaysHolder, false) as TextView
                        day.text = dayLetters[it]

                        val isDayChecked = temporaryAlarm.days and pow != 0
                        day.background = getProperDayDrawable(isDayChecked)

                        day.setTextColor(if (isDayChecked) config.backgroundColor else config.textColor)
                        day.setOnClickListener {
                            EasyDiaryDbHelper.beginTransaction()
                            val selectDay = temporaryAlarm.days and pow == 0
                            temporaryAlarm.days = if (selectDay) {
                                temporaryAlarm.days.addBit(pow)
                            } else {
                                temporaryAlarm.days.removeBit(pow)
                            }
                            day.background = getProperDayDrawable(selectDay)
                            day.setTextColor(if (selectDay) config.backgroundColor else config.textColor)
                            textAlarmDays.text = getSelectedDaysString(temporaryAlarm.days)
                            EasyDiaryDbHelper.commitTransaction()
                        }
                        linearAlarmDaysHolder.addView(day)
                    }
                    textAlarmDays.text = getSelectedDaysString(temporaryAlarm.days)
                    textAlarmDays.setTextColor(config.textColor)
                    switchAlarm.isChecked = temporaryAlarm.isEnabled
                    editAlarmDescription.setText(temporaryAlarm.label)
                    textAlarmTime.text = getFormattedTime(temporaryAlarm.timeInMinutes * 60, false, true)
                    textAlarmTime.setOnClickListener {
                        TimePickerDialog(this@activity, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                            temporaryAlarm.timeInMinutes = hourOfDay * 60 + minute
                            textAlarmTime.text = getFormattedTime(temporaryAlarm.timeInMinutes * 60, false, true)
                        }, temporaryAlarm.timeInMinutes / 60, temporaryAlarm.timeInMinutes % 60, DateFormat.is24HourFormat(this@activity)).show()
                    }
                    switchAlarm.setOnCheckedChangeListener { _, isChecked ->
                        temporaryAlarm.isEnabled = isChecked
                    }
                    when (storedAlarm == null) {
                        true -> { imageDeleteAlarm.visibility = View.GONE }
                        false -> {
                            imageDeleteAlarm.setOnClickListener {
                                showAlertDialog(
                                    "Are you sure you want to delete the selected schedule?",
                                    DialogInterface.OnClickListener { _, _ ->
                                        cancelAlarmClock(temporaryAlarm)
                                        alertDialog?.dismiss()
                                        EasyDiaryDbHelper.beginTransaction()
                                        storedAlarm.deleteFromRealm()
                                        EasyDiaryDbHelper.commitTransaction()
                                        updateAlarmList()
                                    },
                                    { _, _ -> },
                                    DialogMode.WARNING
                                )
                            }
                        }
                    }
                    when (temporaryAlarm.workMode) {
                        Alarm.WORK_MODE_DIARY_WRITING -> radioDiaryWriting.isChecked = true
                        Alarm.WORK_MODE_DIARY_BACKUP_LOCAL -> radioDiaryBackupLocal.isChecked = true
                        Alarm.WORK_MODE_DIARY_BACKUP_GMS -> radioDiaryBackupGms.isChecked = true
                        Alarm.WORK_MODE_CALENDAR_SCHEDULE_SYNC -> radioCalendarScheduleSync.isChecked = true
                    }
                    radioGroupWorkMode.setOnCheckedChangeListener { _, i ->
                        when (i) {
                            R.id.radio_diary_writing -> temporaryAlarm.workMode = Alarm.WORK_MODE_DIARY_WRITING
                            R.id.radio_diary_backup_local -> temporaryAlarm.workMode = Alarm.WORK_MODE_DIARY_BACKUP_LOCAL
                            R.id.radio_diary_backup_gms -> temporaryAlarm.workMode = Alarm.WORK_MODE_DIARY_BACKUP_GMS
                            R.id.radio_calendar_schedule_sync -> temporaryAlarm.workMode = Alarm.WORK_MODE_CALENDAR_SCHEDULE_SYNC
                        }
                    }

                    root.setBackgroundColor(config.backgroundColor)
                    this@activity.initTextSize(root)
                    updateTextColors(root)
                    updateAppViews(root)
                    FontUtils.setFontsTypeface(this@activity, null, root)

                    if (BuildConfig.FLAVOR == "foss") radioDiaryBackupGms.visibility = View.GONE
                }

                alertDialog = builder.create().apply {
                    updateAlertDialog(this, null, dialogAlarmBinding.root, getString(R.string.preferences_category_schedule))
                    getButton(AlertDialog.BUTTON_POSITIVE).run {
                        setOnClickListener {
                            when {
                                dialogAlarmBinding.textAlarmDays.text.isEmpty() -> {
                                    toast("Please select days.")
                                }
                                dialogAlarmBinding.editAlarmDescription.text.isEmpty() -> {
                                    dialogAlarmBinding.editAlarmDescription.run {
                                        requestFocus()
                                        toast("Please input schedule description.")
                                        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                                    }
                                }
                                else -> {
                                    // update alarm schedule
                                    if (temporaryAlarm.isEnabled) {
                                        scheduleNextAlarm(temporaryAlarm, true)
                                    } else {
                                        cancelAlarmClock(temporaryAlarm)
                                    }

                                    // save alarm
                                    temporaryAlarm.label = dialogAlarmBinding?.editAlarmDescription?.text.toString()
                                    EasyDiaryDbHelper.updateAlarmBy(temporaryAlarm)
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

    private fun updateAlarmList() {
        mAlarmList.run {
            clear()
            addAll(EasyDiaryDbHelper.findAlarmAll())
            mBinding.infoMessage.visibility = if (this.isEmpty()) View.VISIBLE else View.GONE
        }
        mAlarmAdapter.notifyDataSetChanged()
    }

    private fun getProperDayDrawable(selected: Boolean): Drawable {
        val drawableId = if (selected) R.drawable.bg_circle_filled else R.drawable.bg_circle_stroke
        val drawable = ContextCompat.getDrawable(mActivity, drawableId)
        drawable!!.applyColorFilter(mActivity.config.textColor)
        return drawable
    }

    private fun initProperties() {
        mActivity.config.use24HourFormat = false
    }

    companion object {
        const val ALARM_ID = "alarm_id"
    }

    class SpacesItemDecoration(private val space: Int) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {
        override fun getItemOffsets(outRect: Rect, view: View, parent: androidx.recyclerview.widget.RecyclerView, state: androidx.recyclerview.widget.RecyclerView.State) {
            val position = parent.getChildAdapterPosition(view)
            when (position == 0) {
                true -> outRect.top = 0
                false -> outRect.top = space
            }
        }
    }
}