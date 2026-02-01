package me.blog.korn123.easydiary.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.zhpan.bannerview.BannerViewPager
import com.zhpan.bannerview.constants.IndicatorGravity
import com.zhpan.bannerview.constants.PageStyle
import me.blog.korn123.commons.utils.EasyDiaryUtils.applyFilter
import me.blog.korn123.easydiary.activities.DiaryReadingActivity
import me.blog.korn123.easydiary.adapters.DiaryDashboardItemAdapter
import me.blog.korn123.easydiary.databinding.FragmentDiaryBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.extensions.spToPixelFloatValue
import me.blog.korn123.easydiary.helper.DIARY_SEQUENCE
import me.blog.korn123.easydiary.helper.DiaryComponentConstants
import me.blog.korn123.easydiary.helper.TransitionHelper
import me.blog.korn123.easydiary.models.Diary
import me.blog.korn123.easydiary.views.FigureIndicatorView

class DiaryFragment : Fragment() {
    /***************************************************************************************************
     *   global properties
     *
     ***************************************************************************************************/
    private lateinit var mBinding: FragmentDiaryBinding
    private lateinit var mBannerDiary: BannerViewPager<Diary>
    private var mDiaryList: ArrayList<Diary> = arrayListOf()

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        mBinding = FragmentDiaryBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

//        mDiaryDashboardItemAdapter = DiaryDashboardItemAdapter(requireActivity(), mDiaryList, {
//            val detailIntent = Intent(requireContext(), DiaryReadingActivity::class.java)
//            detailIntent.putExtra(DIARY_SEQUENCE, it.sequence)
//            detailIntent.putExtra(SELECTED_SEARCH_QUERY, mDiaryDashboardItemAdapter?.currentQuery)
//            detailIntent.putExtra(SELECTED_SYMBOL_SEQUENCE, 0)
//            TransitionHelper.startActivityWithTransition(requireActivity(), detailIntent)
//        }, {})

//        mBinding.recyclerDiary.run {
//            adapter = mDiaryDashboardItemAdapter
//            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//            addItemDecoration(
//                GridItemDecorationDiaryMain(
//                    resources.getDimensionPixelSize(R.dimen.component_margin_small),
//                    requireActivity()
//                )
//            )
//        }

        mBinding.textTitle.text =
            when (arguments?.getString(DiaryComponentConstants.MODE_FLAG, DiaryComponentConstants.MODE_PREVIOUS_100)) {
                DiaryComponentConstants.MODE_TASK_TODO -> "Open Task"
                DiaryComponentConstants.MODE_TASK_DOING -> "DOING"
                DiaryComponentConstants.MODE_TASK_DONE -> "Closed Task"
                DiaryComponentConstants.MODE_TASK_CANCEL -> "CANCEL"
                DiaryComponentConstants.MODE_FUTURE -> "Future"
                else -> "Previous 100"
            }
        setupDiary()
    }

    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).postDelayed({ updateDiary() }, 300)
    }

    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun setupDiary() {
        mBannerDiary =
            (mBinding.bannerViewPagerDiary as BannerViewPager<Diary>).apply {
                setLifecycleRegistry(lifecycle)
                adapter = DiaryDashboardItemAdapter(requireActivity())
                setAutoPlay(false)
                setInterval(3000)
                setScrollDuration(1000)
                setPageMargin(requireContext().dpToPixel(0F))
//            setPageStyle(PageStyle.MULTI_PAGE_SCALE)
                setRevealWidth(0, requireContext().dpToPixel(30F))
//            setIndicatorVisibility(View.INVISIBLE)
//            removeDefaultPageTransformer()

                when (arguments?.getString(DiaryComponentConstants.MODE_FLAG, DiaryComponentConstants.MODE_PREVIOUS_100)) {
                    DiaryComponentConstants.MODE_PREVIOUS_100 -> {
                        setPageMargin(requireContext().dpToPixel(0F))
                        setPageStyle(PageStyle.MULTI_PAGE_SCALE)
                        setRevealWidth(requireContext().dpToPixel(0F), requireContext().dpToPixel(20F))
                        removeDefaultPageTransformer()
                    }

                    else -> {
                        setPageMargin(requireContext().dpToPixel(0F))
                        setPageStyle(PageStyle.MULTI_PAGE_SCALE)
                        setRevealWidth(requireContext().dpToPixel(0F), requireContext().dpToPixel(20F))
                        removeDefaultPageTransformer()
                    }
                }

                FigureIndicatorView(requireContext()).apply {
                    setTextSize(requireContext().spToPixelFloatValue(12F).toInt())
                    setBackgroundColor(config.primaryColor)
                    setIndicatorGravity(IndicatorGravity.END)
                    setIndicatorView(this)
                }

                setOnPageClickListener { _, position ->
                    TransitionHelper.startActivityWithTransition(
                        requireActivity(),
                        Intent(requireContext(), DiaryReadingActivity::class.java).apply {
                            putExtra(DIARY_SEQUENCE, mDiaryList[position].sequence)
                            putExtra(DiaryComponentConstants.MODE_FLAG, arguments?.getString(DiaryComponentConstants.MODE_FLAG, DiaryComponentConstants.MODE_PREVIOUS_100))
                        },
                    )
                }
                registerOnPageChangeCallback(
                    object : OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)
                            Handler(Looper.getMainLooper()).postDelayed({
                                mBannerDiary.adapter.notifyDataSetChanged()
                            }, 300)
                        }
                    },
                )
                create()
            }
    }

    private fun updateDiary() {
        mDiaryList.clear()
        mDiaryList.addAll(applyFilter(arguments?.getString(DiaryComponentConstants.MODE_FLAG, DiaryComponentConstants.MODE_PREVIOUS_100)))
        mBinding.layoutDiaryContainer.visibility = if (mDiaryList.isNotEmpty()) View.VISIBLE else View.GONE
        mBannerDiary.data.clear()
        mBannerDiary.addData(mDiaryList)
    }
}
