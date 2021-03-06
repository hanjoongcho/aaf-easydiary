package me.blog.korn123.easydiary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.aafactory.commons.utils.DateUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.FragmentDashboardSummaryBinding
import me.blog.korn123.easydiary.extensions.config

class DashBoardSummaryFragment : androidx.fragment.app.Fragment() {
    private lateinit var mBinding: FragmentDashboardSummaryBinding

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentDashboardSummaryBinding.inflate(layoutInflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let { ctx ->
            val diaryBackupUsingGMSMillis = ctx.config.diaryBackupGoogle
            mBinding.diaryBackupUsingGMS.text = when {
                diaryBackupUsingGMSMillis > 0L -> DateUtils.getFullPatternDateWithTimeAndSeconds(diaryBackupUsingGMSMillis)
                else -> getString(R.string.dashboard_backup_guide_message)
            }

            val photoBackupUsingGMSMillis = ctx.config.photoBackupGoogle
            mBinding.attachedPhotoBackupUsingGMS.text = when {
                photoBackupUsingGMSMillis > 0L -> DateUtils.getFullPatternDateWithTimeAndSeconds(photoBackupUsingGMSMillis)
                else -> getString(R.string.dashboard_backup_guide_message)
            }

            val diaryBackupUsingLocal = ctx.config.diaryBackupLocal
            mBinding.diaryBackupLocal.text = when {
                diaryBackupUsingLocal > 0L -> DateUtils.getFullPatternDateWithTimeAndSeconds(diaryBackupUsingLocal)
                else -> getString(R.string.dashboard_backup_guide_message)
            }
        }
    }
}
