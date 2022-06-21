package me.blog.korn123.easydiary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.blog.korn123.commons.utils.DateUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.FragmentDashboardSummaryBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.getDefaultDisplay
import me.blog.korn123.easydiary.extensions.isLandScape
import java.text.SimpleDateFormat

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
            requireActivity().run {
                val scaleFactor = if (isLandScape()) 0.5F else 1F
                (getDefaultDisplay().x * 0.8).toInt().also {
                    val width = it.times(scaleFactor).toInt()
                    mBinding.cardDriveBackup.layoutParams.width = width
                    mBinding.cardLocalBackup.layoutParams.width = width
                }
            }
            val diaryBackupUsingGMSMillis = ctx.config.diaryBackupGoogle
            mBinding.diaryBackupUsingGMS.text = when {
                diaryBackupUsingGMSMillis > 0L -> DateUtils.getDateTimeStringFromTimeMillis(diaryBackupUsingGMSMillis, SimpleDateFormat.FULL, SimpleDateFormat.FULL)
                else -> getString(R.string.dashboard_backup_guide_message)
            }

            val photoBackupUsingGMSMillis = ctx.config.photoBackupGoogle
            mBinding.attachedPhotoBackupUsingGMS.text = when {
                photoBackupUsingGMSMillis > 0L -> DateUtils.getDateTimeStringFromTimeMillis(photoBackupUsingGMSMillis, SimpleDateFormat.FULL, SimpleDateFormat.FULL)
                else -> getString(R.string.dashboard_backup_guide_message)
            }

            val diaryBackupUsingLocal = ctx.config.diaryBackupLocal
            mBinding.diaryBackupLocal.text = when {
                diaryBackupUsingLocal > 0L -> DateUtils.getDateTimeStringFromTimeMillis(diaryBackupUsingLocal, SimpleDateFormat.FULL, SimpleDateFormat.FULL)
                else -> getString(R.string.dashboard_backup_guide_message)
            }
        }
    }
}
