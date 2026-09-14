package me.blog.korn123.easydiary.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.gestures.forEach
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.zhpan.bannerview.BannerViewPager
import com.zhpan.bannerview.constants.IndicatorGravity
import com.zhpan.bannerview.constants.PageStyle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.activities.DiaryReadingActivity
import me.blog.korn123.easydiary.adapters.HistoryAdapter
import me.blog.korn123.easydiary.databinding.FragmentPhotoHighlightBinding
import me.blog.korn123.easydiary.domain.model.History
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.extensions.spToPixelFloatValue
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.PhotoHighlightConstants
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.viewmodels.DiaryViewModel
import me.blog.korn123.easydiary.views.FigureIndicatorView
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class PhotoHighlightFragment : androidx.fragment.app.Fragment() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: FragmentPhotoHighlightBinding
    private lateinit var mBannerHistory: BannerViewPager<History>
    var togglePhotoHighlightCallback: ((isVisible: Boolean) -> Unit)? = null
    private val diaryViewModel: DiaryViewModel by viewModels()

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        mBinding = FragmentPhotoHighlightBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupHistory()
    }

    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).post { updateHistory() }
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun setupHistory() {
        mBannerHistory =
            (mBinding.bannerHistory as BannerViewPager<History>).apply {
                setLifecycleRegistry(lifecycle)
                adapter = HistoryAdapter()
                setAutoPlay(arguments?.getBoolean(PhotoHighlightConstants.AUTO_PLAY) ?: false)
                setInterval(3000)
                setScrollDuration(1000)
                setPageMargin(requireContext().dpToPixel(arguments?.getFloat(PhotoHighlightConstants.PAGE_MARGIN) ?: 10F))
                setPageStyle(arguments?.getInt(PhotoHighlightConstants.PAGE_STYLE) ?: PageStyle.MULTI_PAGE_SCALE)
                setRevealWidth(requireContext().dpToPixel(arguments?.getFloat(PhotoHighlightConstants.REVEAL_WIDTH) ?: 10F))
                FigureIndicatorView(requireContext()).apply {
                    setRadius(resources.getDimensionPixelOffset(R.dimen.dp_18))
                    setTextSize(requireContext().spToPixelFloatValue(12F).toInt())
                    setBackgroundColor(config.primaryColor)
                    setIndicatorGravity(IndicatorGravity.END)
                    setIndicatorView(this)
                }
            }
    }

    private fun updateHistory() {
        lifecycleScope.launch {
            if (config.enablePhotoHighlight) {
                when (mBannerHistory.adapter.itemCount == 0) {
                    true -> {
                        diaryViewModel.findOldestDiary()?.let { oldestDiary ->
                            val historyItems = mutableListOf<History>()
                            val oneDayMillis: Long = 1000 * 60 * 60 * 24
                            val oneYearDays = 365
                            val betweenMillis = System.currentTimeMillis().minus(oldestDiary.currentTimeMillis)
                            val betweenDays = betweenMillis / oneDayMillis

                            // makeHistory를 suspend function에서 결과를 반환하는 일반 함수 형태로 생각하거나
                            // 병렬 처리를 위해 deferred list를 사용합니다.
                            val deferredHistories = mutableListOf<kotlinx.coroutines.Deferred<List<History>>>()

                            suspend fun fetchHistoryForPeriod(
                                pastMillis: Long,
                                historyTag: String,
                            ): List<History> {
                                val periodHistories = mutableListOf<History>()
                                val defaultDayBuffer = 1
                                val noDataDayBufferMaxLoop = 3

                                // 처음부터 4일치 데이터를 한 번에 조회하여 반복 쿼리 방지 시도
                                val maxBufferMillis = pastMillis.plus((defaultDayBuffer + noDataDayBufferMaxLoop) * oneDayMillis)
                                val diaryItems = diaryViewModel.findDiary(null, false, pastMillis, maxBufferMillis)

                                // 가져온 데이터 중 가장 이른 날짜(또는 기준일에 가장 가까운 날짜)의 데이터만 추출
                                if (diaryItems.isNotEmpty()) {
                                    val targetDate = diaryItems.first().currentTimeMillis // findDiary 정렬 기준에 따라 조정 필요
                                    diaryItems.filter { it.currentTimeMillis <= targetDate + oneDayMillis }.forEach { diary ->
                                        diary.photoUrisWithEncryptionPolicy()?.forEach { photoUri ->
                                            periodHistories.add(
                                                History(
                                                    historyTag,
                                                    DateUtils.getDateStringFromTimeMillis(diary.currentTimeMillis, SimpleDateFormat.FULL),
                                                    if (diary.isEncrypt) "" else EasyDiaryUtils.getApplicationDataDirectory(requireContext()) + photoUri.getFilePath(),
                                                    diary.diaryId,
                                                ),
                                            )
                                        }
                                    }
                                }
                                return periodHistories
                            }

                            // 월간 하이라이트 작업 예약
                            for (i in 1..11) {
                                val pastMills = EasyDiaryUtils.convDateToTimeMillis(Calendar.MONTH, i.unaryMinus())
                                if (oldestDiary.currentTimeMillis < pastMills) {
                                    deferredHistories.add(
                                        async {
                                            fetchHistoryForPeriod(pastMills, MessageFormat.format(getString(R.string.monthly_highlight_tag), i))
                                        },
                                    )
                                }
                            }

                            // 연간 하이라이트 작업 예약
                            if (betweenDays > oneYearDays) {
                                for (i in 1..(betweenDays / oneYearDays).toInt()) {
                                    val pastMills = EasyDiaryUtils.convDateToTimeMillis(Calendar.YEAR, i.unaryMinus())
                                    deferredHistories.add(
                                        async {
                                            fetchHistoryForPeriod(pastMills, MessageFormat.format(getString(R.string.yearly_highlight_tag), i))
                                        },
                                    )
                                }
                            }

                            // 모든 작업이 완료될 때까지 기다린 후 결과 합치기
                            val results = deferredHistories.awaitAll()
                            results.forEach { historyItems.addAll(it) }

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
                                            },
                                        )
                                    }
                                    registerOnPageChangeCallback(
                                        object : ViewPager2.OnPageChangeCallback() {
                                            override fun onPageSelected(position: Int) {
                                                super.onPageSelected(position)
//                            toast(historyItems[position].title)
                                                mBinding.textDescription.text = historyItems[position].historyTag
                                            }
                                        },
                                    )
                                    create(historyItems)
                                }
                                mBinding.textDescription.text = historyItems[0].historyTag
                            }
                        }
                    }

                    false -> {}
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
    }
}
