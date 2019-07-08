package me.blog.korn123.easydiary.dialogs

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.widget.TextView
import com.simplemobiletools.commons.extensions.setupDialogStuff
import com.simplemobiletools.commons.models.Release
import me.blog.korn123.easydiary.R

class WhatsNewDialog(val activity: Activity, val releases: List<Release>) {
    init {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_whats_new, null)
        view.findViewById<TextView>(R.id.whats_new_content).text = getNewReleases()

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.ok, null)
                .create().apply {
                    activity.setupDialogStuff(view, this, R.string.whats_new)
                }
    }

    private fun getNewReleases(): String {
        val sb = StringBuilder()

        releases.forEach {
            val parts = activity.getString(it.textId).split("\n").map(String::trim)
            parts.forEachIndexed { index, description -> 
                when (index) {
                    0 -> sb.append("* $description\n") 
                    else -> sb.append("- $description\n")
                }
            }
            sb.append("\n")
        }

        return sb.toString()
    }
}
