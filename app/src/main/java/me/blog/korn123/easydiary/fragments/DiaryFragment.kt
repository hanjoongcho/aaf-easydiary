package me.blog.korn123.easydiary.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.zhpan.bannerview.BannerViewPager
import com.zhpan.bannerview.constants.IndicatorGravity
import com.zhpan.bannerview.constants.PageStyle
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.adapters.DiaryDashboardItemAdapter
import me.blog.korn123.easydiary.databinding.FragmentDiaryBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.dpToPixel
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
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
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentDiaryBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

        mBinding.textTitle.text = when (arguments?.getString(MODE_FLAG, MODE_PREVIOUS_100)) {
            MODE_TASK -> "Task"
            else -> "Previous 100"
        }
        setupDiary()
    }

    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).post { updateDiary() }
    }


    /***************************************************************************************************
     *   etc functions
     *
     ***************************************************************************************************/
    private fun setupDiary() {
        mBannerDiary = (mBinding.bannerViewPagerDiary as BannerViewPager<Diary>).apply {
            setLifecycleRegistry(lifecycle)
            adapter = DiaryDashboardItemAdapter()
            setAutoPlay(false)
            setInterval(3000)
            setScrollDuration(800)
            setPageMargin(requireContext().dpToPixel(5F))
            setPageStyle(PageStyle.MULTI_PAGE_SCALE)
            setRevealWidth(requireContext().dpToPixel(20F))
            FigureIndicatorView(requireContext()).apply {
                setRadius(resources.getDimensionPixelOffset(R.dimen.dp_18))
                setTextSize(resources.getDimensionPixelOffset(R.dimen.sp_13))
                setBackgroundColor(config.primaryColor)
                setIndicatorGravity(IndicatorGravity.END)
                setIndicatorView(this)
            }
        }
    }

    private fun updateDiary() {
        mDiaryList.clear()
        val diaryList: List<Diary> = when (arguments?.getString(MODE_FLAG, MODE_PREVIOUS_100)) {
            MODE_TASK -> EasyDiaryDbHelper.findDiary(null, config.diarySearchQueryCaseSensitive, 0, 0, 0).filter { item -> item.weather in 80..83 }
            else -> EasyDiaryDbHelper.findDiary(null, config.diarySearchQueryCaseSensitive, 0, 0, 0).subList(0, 100)
        }
        mDiaryList.addAll(diaryList)
        mBannerDiary.run {
            create(mDiaryList)
        }
    }

    companion object {
        const val MODE_FLAG = "mode"
        const val MODE_TASK = "task"
        const val MODE_PREVIOUS_100 = "previous100"
    }
}