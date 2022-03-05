package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.simplemobiletools.commons.extensions.toast
import io.github.aafactory.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.databinding.DialogDdayBinding
import me.blog.korn123.easydiary.databinding.ItemDdayAddBinding
import me.blog.korn123.easydiary.databinding.ItemDdayBinding
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.models.DDay
import java.util.*

class DDayAdapter(
    val activity: Activity,
    private val dDayItems: MutableList<DDay>,
    private val saveDDayCallback: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType == 0) {
            true -> DDayViewHolder(ItemDdayBinding.inflate(activity.layoutInflater))
            false -> DDayAddViewHolder(ItemDdayAddBinding.inflate(activity.layoutInflater))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (dDayItems.size == position.plus(1)) {
            true -> 1
            false -> 0
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return when (dDayItems.size == position.plus(1)) {
            true -> (holder as DDayAddViewHolder).bindTo(dDayItems[position])
            false -> (holder as DDayViewHolder).bindTo(dDayItems[position])
        }
    }

    fun openDDayDialog(temporaryDDay: DDay, storedDDay: DDay? = null) {
        activity.run activity@ {
            val dDayBinding = DialogDdayBinding.inflate(layoutInflater).apply {
                val calendar = Calendar.getInstance(Locale.getDefault())
                when (storedDDay == null) {
                    true -> {
                        title.hint = "Enter the name of the target date."

                    }
                    false -> {
                        title.setText(temporaryDDay.title)
                        calendar.timeInMillis = storedDDay.targetTimeStamp
                    }
                }
                var year = calendar.get(Calendar.YEAR)
                var month = calendar.get(Calendar.MONTH)
                var dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                var hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
                var minute = calendar.get(Calendar.MINUTE)

                targetDate.text = DateUtils.getDateStringFromTimeMillis(temporaryDDay.targetTimeStamp)
                targetTime.text = DateUtils.timeMillisToDateTime(temporaryDDay.targetTimeStamp,  DateUtils.TIME_PATTERN)
                remainDays.text = temporaryDDay.getRemainDays()
                root.setBackgroundColor(config.backgroundColor)
                FontUtils.setFontsTypeface(this@activity, this@activity.assets, null, root)

                targetDate.setOnClickListener {
                    val datePickerDialog = DatePickerDialog(this@activity, { _, y, m, d ->
                        year = y
                        month = m
                        dayOfMonth = d
                        temporaryDDay.targetTimeStamp = EasyDiaryUtils.datePickerToTimeMillis(dayOfMonth, month, year, false, hourOfDay, minute)
                        targetDate.text = DateUtils.getDateStringFromTimeMillis(temporaryDDay.targetTimeStamp)
                        targetTime.text = DateUtils.timeMillisToDateTime(temporaryDDay.targetTimeStamp,  DateUtils.TIME_PATTERN)
                        remainDays.text = temporaryDDay.getRemainDays()
                    }, year, month, dayOfMonth)
                    datePickerDialog.show()
                }
                targetTime.setOnClickListener {
                    TimePickerDialog(this@activity, { _, h, m ->
                        hourOfDay = h
                        minute = m
                        temporaryDDay.targetTimeStamp = EasyDiaryUtils.datePickerToTimeMillis(dayOfMonth, month, year, false, hourOfDay, minute)
                        targetTime.text = DateUtils.timeMillisToDateTime(temporaryDDay.targetTimeStamp,  DateUtils.TIME_PATTERN)
                    }, hourOfDay, minute, DateFormat.is24HourFormat(this@activity)).show()
                }
            }
            var alertDialog: AlertDialog? = null
            val builder = AlertDialog.Builder(this).apply {
                setCancelable(false)
                setPositiveButton(getString(android.R.string.ok), null)
                setNegativeButton(getString(android.R.string.cancel)) { _, _ -> alertDialog?.dismiss() }
            }
            alertDialog = builder.create().apply {
                updateAlertDialog(this, null, dDayBinding.root)
                getButton(AlertDialog.BUTTON_POSITIVE).run {
                    setOnClickListener {
                        when {
                            dDayBinding.title.text.isEmpty() -> {
                                toast("Enter the name of the target date.")
                            }
                            else -> {
                                temporaryDDay.title = dDayBinding.title.text.toString()
                                EasyDiaryDbHelper.updateDDayBy(temporaryDDay)
                                dismiss()
                                saveDDayCallback.invoke()
                            }
                        }
                    }
                }
            }
        }
    }

    inner class DDayViewHolder(private val itemDDayBinding: ItemDdayBinding) : RecyclerView.ViewHolder(itemDDayBinding.root) {

        init {
            activity.run {
                initTextSize(itemDDayBinding.root)
                updateTextColors(itemDDayBinding.root)
                updateAppViews(itemDDayBinding.root)
                updateCardViewPolicy(itemDDayBinding.root)
                FontUtils.setFontsTypeface(this, this.assets, null, itemDDayBinding.root)
            }
        }

        fun bindTo(dDay: DDay) {
            EasyDiaryUtils.boldStringForce(activity, itemDDayBinding.remainDays)
            itemDDayBinding.run {
                title.text = dDay.title
                targetDate.text = DateUtils.getDateStringFromTimeMillis(dDay.targetTimeStamp)
                targetTime.text = DateUtils.timeMillisToDateTime(dDay.targetTimeStamp,  DateUtils.TIME_PATTERN)
                remainDays.text = dDay.getRemainDays()
                remainHours.text = "${dDay.getRemainHours()}Hours"
                root.setOnClickListener {
                    openDDayDialog(EasyDiaryDbHelper.duplicateDDayBy(dDay), dDay)
                }
            }
        }
    }

    inner class DDayAddViewHolder(private val itemDDayAddBinding: ItemDdayAddBinding) : RecyclerView.ViewHolder(itemDDayAddBinding.root) {

        init {
            activity.run {
                itemDDayAddBinding.root.also {
                    initTextSize(it)
                    updateTextColors(it)
                    updateAppViews(it)
                    updateCardViewPolicy(it)
                    FontUtils.setFontsTypeface(this, this.assets, null, it)
                }
            }
        }

        fun bindTo(dDay: DDay) {
            itemDDayAddBinding.root.setOnClickListener {
                openDDayDialog(dDay)
            }
        }
    }

    override fun getItemCount(): Int = dDayItems.size
}