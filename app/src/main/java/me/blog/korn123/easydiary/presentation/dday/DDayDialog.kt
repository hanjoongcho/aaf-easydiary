package me.blog.korn123.easydiary.presentation.dday

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.simplemobiletools.commons.extensions.toast
import kotlinx.coroutines.launch
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.DialogDdayBinding
import me.blog.korn123.easydiary.domain.model.DDay
import me.blog.korn123.easydiary.enums.DialogMode
import me.blog.korn123.easydiary.extensions.applicationScope
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dDayRepository
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.extensions.showAlertDialog
import me.blog.korn123.easydiary.extensions.storedTimeFormat
import me.blog.korn123.easydiary.extensions.updateAlertDialog
import me.blog.korn123.easydiary.extensions.updateDrawableColorInnerCardView
import me.blog.korn123.easydiary.extensions.updateTextColors
import me.blog.korn123.easydiary.utils.DateUtils
import me.blog.korn123.easydiary.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.utils.FontUtils
import java.util.Calendar
import java.util.Locale

/**
 * The dialog a card of the D-Day list opens: the name and the target date of a D-Day, its remaining days, and the
 * delete icon of a stored one.
 *
 * [storedDDay] decides what the dialog is for. Given, the dialog edits that D-Day and can delete it; without it,
 * the dialog adds the [temporaryDDay] the card handed over - the `New D-Day` cards do that.
 *
 * The dialog itself is the one the D-Day adapter used to open, moved out of the adapter when the list became
 * Compose, and it keeps working on the same `dialog_dday.xml`: the theme colors, the font of the app, the date and
 * time pickers and the delete confirmation are unchanged.
 */
fun Activity.showDDayDialog(
    temporaryDDay: DDay,
    storedDDay: DDay? = null,
    onChanged: () -> Unit,
) {
    var alertDialog: AlertDialog? = null
    val dDayBinding =
        DialogDdayBinding.inflate(layoutInflater).apply {
            val calendar = Calendar.getInstance(Locale.getDefault())
            when (storedDDay == null) {
                true -> {
                    textTitle.hint = getString(R.string.dashboard_dday_title_hint)
                }

                false -> {
                    textTitle.setText(temporaryDDay.title)
                    calendar.timeInMillis = storedDDay.targetTimeStamp
                }
            }
            var year = calendar.get(Calendar.YEAR)
            var month = calendar.get(Calendar.MONTH)
            var dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            var hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
            var minute = calendar.get(Calendar.MINUTE)

            fun updateDDayInfo() {
                textTargetDate.text = DateUtils.getDateStringFromTimeMillis(temporaryDDay.targetTimeStamp)
                textTargetTime.text = DateUtils.getTimeStringFromTimeMillis(temporaryDDay.targetTimeStamp, timeFormatMode = this@showDDayDialog.storedTimeFormat())
                textDayRemaining.text = temporaryDDay.getOnlyDayRemaining()
                textDayRemainingWithYear.text =
                    temporaryDDay.getOnlyDayRemaining(
                        false,
                        this@showDDayDialog.getString(R.string.year_message_format),
                        this@showDDayDialog.getString(R.string.day_message_format),
                    )
                textTimeRemaining.text =
                    "${temporaryDDay.getDayRemaining(false, this@showDDayDialog.getString(R.string.year_message_format), this@showDDayDialog.getString(R.string.day_message_format))} ${temporaryDDay.getTimeRemaining()}"
            }

            updateDDayInfo()
            updateDrawableColorInnerCardView(imageDeleteDDay)
            root.also {
                initTextSize(it)
                updateTextColors(it)
                it.setBackgroundColor(config.backgroundColor)
                FontUtils.setFontsTypeface(this@showDDayDialog, null, it)
            }

            textTargetDate.setOnClickListener {
                val datePickerDialog =
                    DatePickerDialog(this@showDDayDialog, { _, y, m, d ->
                        year = y
                        month = m
                        dayOfMonth = d
                        temporaryDDay.targetTimeStamp = EasyDiaryUtils.datePickerToTimeMillis(dayOfMonth, month, year, false, hourOfDay, minute)
                        updateDDayInfo()
                    }, year, month, dayOfMonth)
                datePickerDialog.show()
            }
            textTargetTime.setOnClickListener {
                TimePickerDialog(this@showDDayDialog, { _, h, m ->
                    hourOfDay = h
                    minute = m
                    temporaryDDay.targetTimeStamp = EasyDiaryUtils.datePickerToTimeMillis(dayOfMonth, month, year, false, hourOfDay, minute)
                    updateDDayInfo()
                }, hourOfDay, minute, DateFormat.is24HourFormat(this@showDDayDialog)).show()
            }
            when (storedDDay == null) {
                true -> {
                    imageDeleteDDay.visibility = View.GONE
                }

                false -> {
                    imageDeleteDDay.setOnClickListener {
                        showAlertDialog(
                            getString(R.string.dashboard_dday_delete_confirm),
                            { _, _ ->
                                applicationScope.launch {
                                    alertDialog?.dismiss()
                                    dDayRepository.deleteDDay(storedDDay)
                                    onChanged()
                                }
                            },
                            { _, _ -> },
                            DialogMode.WARNING,
                            true,
                            getString(R.string.delete),
                            getString(R.string.delete),
                        )
                    }
                }
            }
        }
    val builder =
        AlertDialog.Builder(this).apply {
            setCancelable(false)
            setPositiveButton(getString(android.R.string.ok), null)
            setNegativeButton(getString(android.R.string.cancel)) { _, _ -> alertDialog?.dismiss() }
        }
    alertDialog =
        builder.create().apply {
            updateAlertDialog(this, null, dDayBinding.root)
            getButton(AlertDialog.BUTTON_POSITIVE).run {
                setOnClickListener {
                    when {
                        dDayBinding.textTitle.text?.isEmpty() ?: true -> {
                            toast(getString(R.string.dashboard_dday_title_hint))
                        }

                        else -> {
                            lifecycleScope.launch {
                                temporaryDDay.title = dDayBinding.textTitle.text.toString()
                                dDayRepository.updateDDay(temporaryDDay)
                                dismiss()
                                onChanged()
                            }
                        }
                    }
                }
            }
        }
}
