package me.blog.korn123.easydiary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.FragmentDashboardSummaryBinding
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.getDashboardCardWidth
import me.blog.korn123.easydiary.utils.DateUtils
import java.text.SimpleDateFormat

class DashBoardSummaryFragment : Fragment() {
    private lateinit var binding: FragmentDashboardSummaryBinding

    /***************************************************************************************************
     *   override functions
     *
     ***************************************************************************************************/
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDashboardSummaryBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        context?.let { ctx ->
            binding.run {
                requireActivity().getDashboardCardWidth(0.8F).also {
                    cardDriveBackup.layoutParams.width = it
                    cardLocalBackup.layoutParams.width = it
                }

                val diaryBackupUsingGMSMillis = ctx.config.diaryBackupGoogle
                diaryBackupUsingGMS.text =
                    when {
                        diaryBackupUsingGMSMillis > 0L -> DateUtils.getDateTimeStringFromTimeMillis(diaryBackupUsingGMSMillis, SimpleDateFormat.FULL, SimpleDateFormat.FULL)
                        else -> getString(R.string.dashboard_backup_guide_message)
                    }

                val photoBackupUsingGMSMillis = ctx.config.photoBackupGoogle
                attachedPhotoBackupUsingGMS.text =
                    when {
                        photoBackupUsingGMSMillis > 0L -> DateUtils.getDateTimeStringFromTimeMillis(photoBackupUsingGMSMillis, SimpleDateFormat.FULL, SimpleDateFormat.FULL)
                        else -> getString(R.string.dashboard_backup_guide_message)
                    }

                val diaryBackupUsingLocal = ctx.config.diaryBackupLocal
                diaryBackupLocal.text =
                    when {
                        diaryBackupUsingLocal > 0L -> DateUtils.getDateTimeStringFromTimeMillis(diaryBackupUsingLocal, SimpleDateFormat.FULL, SimpleDateFormat.FULL)
                        else -> getString(R.string.dashboard_backup_guide_message)
                    }
            }
        }
    }
}
