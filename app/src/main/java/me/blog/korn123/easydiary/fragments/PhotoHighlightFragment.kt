package me.blog.korn123.easydiary.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
import me.blog.korn123.easydiary.presentation.diary.DiaryViewModel
import me.blog.korn123.easydiary.views.FigureIndicatorView

@AndroidEntryPoint
class PhotoHighlightFragment : Fragment() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var binding: FragmentPhotoHighlightBinding
    private lateinit var bannerHistory: BannerViewPager<History>
    var togglePhotoHighlightCallback: ((isVisible: Boolean) -> Unit)? = null

    // Use viewModels() to scoping ViewModel for diary operations
    private val diaryViewModel: DiaryViewModel by viewModels()

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPhotoHighlightBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupHistory()
        observePhotoHighlight()
    }

    private val isEnabledPhotoHighlight: Boolean
        get() {
            val isDashboard = arguments?.getBoolean(PhotoHighlightConstants.IS_DASHBOARD) ?: false
            return if (isDashboard) config.enablePhotoHighlightInDashboard else config.enablePhotoHighlightInDiaryMain
        }

    override fun onResume() {
        super.onResume()
        if (isEnabledPhotoHighlight) {
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
        bannerHistory =
            (binding.bannerHistory as BannerViewPager<History>).apply {
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
                    if (!isEnabledPhotoHighlight) {
                        initDefaultSettings()
                        return@collect
                    }

                    if (historyItems.isNotEmpty()) {
                        togglePhotoHighlightCallback?.invoke(true)
                        binding.layoutBannerContainer.visibility = View.VISIBLE
                        bannerHistory.run {
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
                                        binding.textDescription.text = historyItems[position].historyTag
                                    }
                                },
                            )
                            create(historyItems)
                        }
                        binding.textDescription.text = historyItems[0].historyTag
                    }
                }
            }
        }
    }

    private fun initDefaultSettings() {
        togglePhotoHighlightCallback?.invoke(false)
        binding.run {
            layoutBannerContainer.visibility = View.GONE
            bannerHistory.refreshData(mutableListOf<History>())
        }
    }
}
