package com.simplemobiletools.commons.dialogs

import android.app.Activity
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.models.Release
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.databinding.DialogWhatsNewBinding

class WhatsNewDialog(
    val activity: Activity,
    val releases: List<Release>,
) {
    private lateinit var binding: DialogWhatsNewBinding

    init {
        binding =
            DialogWhatsNewBinding.inflate(LayoutInflater.from(activity)).apply {

                whatsNewContent.text = getNewReleases()
            }

        AlertDialog
            .Builder(activity)
            .setPositiveButton(R.string.ok, null)
            .create()
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.whats_new)
            }
    }

    private fun getNewReleases(): String {
        val sb = StringBuilder()

        releases.forEach {
            val parts = activity.getString(it.textId).split("\n").map(String::trim)
            parts.forEach {
                sb.append("- $it\n")
            }
        }

        return sb.toString()
    }
}
