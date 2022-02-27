package me.blog.korn123.easydiary.dialogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.zhpan.bannerview.constants.PageStyle
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ActivityDashboardBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.fragments.DailySymbolFragment
import me.blog.korn123.easydiary.fragments.DashBoardSummaryFragment
import me.blog.korn123.easydiary.fragments.PhotoHighlightFragment

class DashboardDialogFragment : DialogFragment() {
    private lateinit var mBinding: ActivityDashboardBinding

    override fun onStart() {
        super.onStart()
        dialog?.run {
            window?.also {
                it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                it.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = ActivityDashboardBinding.inflate(layoutInflater)


        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.run {
            appBar.visibility = View.GONE
            close.visibility = View.VISIBLE
            close.setOnClickListener { dismiss() }

            if (savedInstanceState == null) {
                childFragmentManager.beginTransaction().run {
                    // PhotoHighlight
                    replace(R.id.photoHighlight, PhotoHighlightFragment().apply {
                        arguments = Bundle().apply {
                            putInt(PhotoHighlightFragment.PAGE_STYLE, PageStyle.MULTI_PAGE_SCALE)
                            putFloat(PhotoHighlightFragment.REVEAL_WIDTH, 20F)
                            putFloat(PhotoHighlightFragment.PAGE_MARGIN, 5F)
                        }
                    })

                    // DashBoardSummary
                    replace(R.id.summary, DashBoardSummaryFragment())

                    // Daily Symbol
                    replace(R.id.dashboard_daily_symbol, DailySymbolFragment())

                    // Commit
                    commit()
                }
            }

//            childFragmentManager.executePendingTransactions()
//            EasyDiaryUtils.disableTouchEvent(dashboardDimmer)
            dashboardDimmer.visibility = View.GONE
            dashboardProgress.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        mBinding.run {
            root.setBackgroundColor(requireActivity().config.screenBackgroundColor)
            FontUtils.setFontsTypeface(requireContext(), null, root, true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}