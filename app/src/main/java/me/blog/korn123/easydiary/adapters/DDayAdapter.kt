package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.text.format.DateFormat
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.simplemobiletools.commons.extensions.toast
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.DialogDdayBinding
import me.blog.korn123.easydiary.databinding.ItemDdayAddBinding
import me.blog.korn123.easydiary.databinding.ItemDdayBinding
import me.blog.korn123.easydiary.extensions.*
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.models.DDay
import java.text.SimpleDateFormat
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

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        val lp = holder.itemView.layoutParams
        if (lp is FlexboxLayoutManager.LayoutParams) {
            if (holder is DDayViewHolder) lp.flexGrow = 1F
        }
    }

    fun openDDayDialog(temporaryDDay: DDay, storedDDay: DDay? = null) {
        activity.run activity@ {
            var alertDialog: AlertDialog? = null
            val dDayBinding = DialogDdayBinding.inflate(layoutInflater).apply {
                val calendar = Calendar.getInstance(Locale.getDefault())
                when (storedDDay == null) {
                    true -> {
                        textTitle.hint = "Enter the name of the target date."

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
                    textTargetTime.text = DateUtils.getTimeStringFromTimeMillis(temporaryDDay.targetTimeStamp)
                    textDayRemaining.text = temporaryDDay.getDayRemaining()
                    textDayRemainingWithYear.text = temporaryDDay.getDayRemaining(false, activity.getString(R.string.year_message_format), activity.getString(R.string.day_message_format))
                    textTimeRemaining.text = temporaryDDay.getTimeRemaining()
                }

                updateDDayInfo()
                updateDrawableColorInnerCardView(imageDeleteDDay)
                root.also {
                    initTextSize(it)
                    updateTextColors(it)
                    it.setBackgroundColor(config.backgroundColor)
                    FontUtils.setFontsTypeface(this@activity, null, it)
                }

                textTargetDate.setOnClickListener {
                    val datePickerDialog = DatePickerDialog(this@activity, { _, y, m, d ->
                        year = y
                        month = m
                        dayOfMonth = d
                        temporaryDDay.targetTimeStamp = EasyDiaryUtils.datePickerToTimeMillis(dayOfMonth, month, year, false, hourOfDay, minute)
                        updateDDayInfo()
                    }, year, month, dayOfMonth)
                    datePickerDialog.show()
                }
                textTargetTime.setOnClickListener {
                    TimePickerDialog(this@activity, { _, h, m ->
                        hourOfDay = h
                        minute = m
                        temporaryDDay.targetTimeStamp = EasyDiaryUtils.datePickerToTimeMillis(dayOfMonth, month, year, false, hourOfDay, minute)
                        updateDDayInfo()
                    }, hourOfDay, minute, DateFormat.is24HourFormat(this@activity)).show()
                }
                when (storedDDay == null) {
                    true -> imageDeleteDDay.visibility = View.GONE
                    false -> {
                        imageDeleteDDay.setOnClickListener {
                            showAlertDialog("Are you sure you want to delete the selected D-Day?", { _, _ ->
                                alertDialog?.dismiss()
                                EasyDiaryDbHelper.beginTransaction()
                                storedDDay.deleteFromRealm()
                                EasyDiaryDbHelper.commitTransaction()
                                saveDDayCallback.invoke()
                            }, null)
                        }
                    }
                }
            }
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
                            dDayBinding.textTitle.text.isEmpty() -> {
                                toast("Enter the name of the target date.")
                            }
                            else -> {
                                temporaryDDay.title = dDayBinding.textTitle.text.toString()
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
                FontUtils.setFontsTypeface(this, null, itemDDayBinding.root)
            }
        }

        fun bindTo(dDay: DDay) {
            EasyDiaryUtils.boldStringForce(itemDDayBinding.textDayRemaining)
            itemDDayBinding.run {
                val targetDateString = DateUtils.getDateStringFromTimeMillis(dDay.targetTimeStamp, SimpleDateFormat.MEDIUM)
                val currentDateString = DateUtils.getDateStringFromTimeMillis(System.currentTimeMillis(), SimpleDateFormat.MEDIUM)
                textTitle.text = dDay.title
                textTargetDate.text = targetDateString
//                textTargetTime.text = DateUtils.getTimeStringFromTimeMillis(dDay.targetTimeStamp, SimpleDateFormat.SHORT)
                textDayRemaining.text = if (targetDateString == currentDateString) "D-Day" else dDay.getDayRemaining()
                when {
                    dDay.getDayRemaining().matches(Regex("^D－0$|^D＋0$")) -> {
                        imgLightRed.alpha = 0.1F
                        imgLightOrange.alpha = 1F
                        imgLightGreen.alpha = 0.1F
                    }
                    dDay.getDayRemaining().startsWith("D＋") -> {
                        imgLightRed.alpha = 1F
                        imgLightOrange.alpha = 0.1F
                        imgLightGreen.alpha = 0.1F
                    }
                    else -> {
                        imgLightRed.alpha = 0.1F
                        imgLightOrange.alpha = 0.1F
                        imgLightGreen.alpha = 1F
                    }
                }
//                textDayRemainingWithYear.text = dDay.getDayRemaining(false, activity.getString(R.string.year_message_format), activity.getString(R.string.day_message_format))
//                textTimeRemaining.text = dDay.getTimeRemaining()
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
                    FontUtils.setFontsTypeface(this, null, it)
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