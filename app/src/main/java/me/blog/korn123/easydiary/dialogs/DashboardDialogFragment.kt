package me.blog.korn123.easydiary.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.zhpan.bannerview.constants.PageStyle
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.ActivityDashboardBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.fragments.DashBoardSummaryFragment
import me.blog.korn123.easydiary.fragments.PhotoHighlightFragment

class DashboardDialogFragment : DialogFragment() {
    private lateinit var mBinding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        dialog?.run {
            window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
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
            close.setOnClickListener { dismiss() }

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

                // Commit
                commit()
            }





        }
    }

    override fun onResume() {
        super.onResume()
        mBinding.run {
            root.setBackgroundColor(requireActivity().config.screenBackgroundColor)
            FontUtils.setFontsTypeface(requireContext(), null, root, true)
        }
    }
}