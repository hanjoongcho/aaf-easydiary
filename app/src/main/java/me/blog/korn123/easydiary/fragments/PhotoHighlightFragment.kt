package me.blog.korn123.easydiary.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.zhpan.bannerview.BannerViewPager
import com.zhpan.bannerview.constants.IndicatorGravity
import com.zhpan.bannerview.constants.PageStyle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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

@AndroidEntryPoint
class PhotoHighlightFragment : androidx.fragment.app.Fragment() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: FragmentPhotoHighlightBinding
    private lateinit var mBannerHistory: BannerViewPager<History>
    var togglePhotoHighlightCallback: ((isVisible: Boolean) -> Unit)? = null

    // 여러 Activity 및 Fragment 간 ViewModel 스코프를 유지하고 싱글톤 매니저와 연동되도록 activityViewModels() 사용
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
        observePhotoHighlight()
    }

    override fun onResume() {
        super.onResume()
        if (config.enablePhotoHighlight) {
            diaryViewModel.updatePhotoHighlightIfNeeded()
        } else {
            initDefaultSettings()
        }
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

    private fun observePhotoHighlight() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                diaryViewModel.photoHighlightList.collect { historyItems ->
                    if (!config.enablePhotoHighlight) {
                        initDefaultSettings()
                        return@collect
                    }

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
        }
    }

    private fun initDefaultSettings() {
        togglePhotoHighlightCallback?.invoke(false)
        mBinding.run {
            layoutBannerContainer.visibility = View.GONE
            mBannerHistory.refreshData(mutableListOf())
        }
    }
}
