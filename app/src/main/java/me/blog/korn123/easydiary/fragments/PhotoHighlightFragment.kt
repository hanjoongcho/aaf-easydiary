package me.blog.korn123.easydiary.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.zhpan.bannerview.BannerViewPager
import com.zhpan.bannerview.constants.IndicatorGravity
import com.zhpan.bannerview.constants.PageStyle
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryReadingActivity
import me.blog.korn123.easydiary.adapters.HistoryAdapter
import me.blog.korn123.easydiary.databinding.FragmentPhotoHighlightBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.models.History
import me.blog.korn123.easydiary.views.FigureIndicatorView
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.util.*

class PhotoHighlightFragment : androidx.fragment.app.Fragment() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: FragmentPhotoHighlightBinding
    private lateinit var mBannerHistory: BannerViewPager<History>
    var togglePhotoHighlightCallback: ((isVisible: Boolean) -> Unit)? = null


    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentPhotoHighlightBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupHistory()
    }

    override fun onResume() {
        super.onResume()
        updateHistory()
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun setupHistory() {
        mBannerHistory = (mBinding.bannerHistory as BannerViewPager<History>).apply {
            setLifecycleRegistry(lifecycle)
            adapter = HistoryAdapter()
            setAutoPlay(arguments?.getBoolean(AUTO_PLAY) ?: false)
            setInterval(3000)
            setScrollDuration(800)
            setPageMargin(requireContext().dpToPixel(arguments?.getFloat(PAGE_MARGIN) ?: 10F))
            setPageStyle(arguments?.getInt(PAGE_STYLE) ?: PageStyle.MULTI_PAGE_SCALE)
            setRevealWidth(requireContext().dpToPixel(arguments?.getFloat(REVEAL_WIDTH) ?: 10F))
            FigureIndicatorView(requireContext()).apply {
                setRadius(resources.getDimensionPixelOffset(R.dimen.dp_18))
                setTextSize(resources.getDimensionPixelOffset(R.dimen.sp_13))
                setBackgroundColor(config.primaryColor)
                setIndicatorGravity(IndicatorGravity.END)
                setIndicatorView(this)
            }
        }
    }

    private fun updateHistory() {
        if (config.enablePhotoHighlight) {
            when (mBannerHistory.adapter.itemCount == 0) {
                true -> {
                    EasyDiaryDbHelper.findOldestDiary()?.let { oldestDiary ->
                        val historyItems = mutableListOf<History>()
                        val oneDayMillis: Long = 1000 * 60 * 60 * 24
                        val oneYearDays = 365
                        val betweenMillis = System.currentTimeMillis().minus(oldestDiary.currentTimeMillis)
                        val betweenDays = betweenMillis / oneDayMillis
                        fun makeHistory(pastMillis: Long, historyTag: String) {
                            val defaultDayBuffer = 1
                            val noDataDayBufferMaxLoop = 3
                            val pastMillisBuffer  = pastMillis.plus(defaultDayBuffer * oneDayMillis)
                            var diaryItems = EasyDiaryDbHelper.findDiary(null, false, pastMillis, pastMillisBuffer)
                            if (diaryItems.isEmpty()) {
                                for (i in 1..noDataDayBufferMaxLoop) {
                                    diaryItems = EasyDiaryDbHelper.findDiary(null, false, pastMillis, pastMillisBuffer.plus(i * oneDayMillis))
                                    if (diaryItems.isNotEmpty()) break
                                }
                            }
                            diaryItems.forEach {
                                it.photoUrisWithEncryptionPolicy()?.forEach { photoUri ->
                                    historyItems.add(
                                        History(
                                            historyTag,
                                            DateUtils.getDateStringFromTimeMillis(it.currentTimeMillis, SimpleDateFormat.FULL),
                                            if (it.isEncrypt) "" else EasyDiaryUtils.getApplicationDataDirectory(requireContext()) + photoUri.getFilePath(),
                                            it.sequence
                                        )
                                    )
                                }
                            }
                        }

                        // 1 month history of less than 1 year
                        for (i in 1..11) {
                            val pastMills = EasyDiaryUtils.convDateToTimeMillis(Calendar.MONTH, i.unaryMinus())
                            if (oldestDiary.currentTimeMillis < pastMills) {
                                makeHistory(pastMills,
                                    MessageFormat.format(getString(R.string.monthly_highlight_tag), i))
                            }
                        }

                        // 1 year history of more than 1 year
                        if (betweenDays > oneYearDays) {
                            for (i in 1..(betweenDays / oneYearDays).toInt()) {
                                makeHistory(EasyDiaryUtils.convDateToTimeMillis(Calendar.YEAR, i.unaryMinus()), MessageFormat.format(getString(R.string.yearly_highlight_tag), i))
                            }
                        }
                        historyItems.reverse()

                        if (historyItems.isNotEmpty()) {
                            togglePhotoHighlightCallback?.invoke(true)
                            mBinding.layoutBannerContainer.visibility = View.VISIBLE
                            mBannerHistory.run {
                                setOnPageClickListener { _, position ->
                                    TransitionHelper.startActivityWithTransition(
                                        requireActivity(),
                                        Intent(requireContext(), DiaryReadingActivity::class.java).apply {
                                            putExtra(DIARY_SEQUENCE, historyItems[position].sequence)
                                        }
                                    )
                                }
                                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                                    override fun onPageSelected(position: Int) {
                                        super.onPageSelected(position)
//                            toast(historyItems[position].title)
                                        mBinding.textDescription.text = historyItems[position].historyTag
                                    }
                                })
                                create(historyItems)
                            }
                            mBinding.textDescription.text = historyItems[0].historyTag
                        }
                    }
                }
            }
        } else {
            // init default settings
            togglePhotoHighlightCallback?.invoke(false)
            mBinding.run {
                layoutBannerContainer.visibility = View.GONE
                mBannerHistory.refreshData(mutableListOf())
            }
        }
    }

    companion object {
        const val PAGE_STYLE = "page_style"
        const val PAGE_MARGIN = "page_margin"
        const val REVEAL_WIDTH = "reveal_width"
        const val AUTO_PLAY = "auto_play"
    }
}