package me.blog.korn123.easydiary.extensions

import android.text.style.ClickableSpan
import android.view.View
import me.blog.korn123.easydiary.utils.ClickableLinkUtils
import me.blog.korn123.easydiary.utils.DiaryLinkTarget

/**
 * A link of a plain text diary. It is drawn like the URLSpan Linkify creates, but it is
 * opened the same way the links of a markdown diary are: a diary inside the app, a phone
 * number in the dialer and a web page in a custom tab (#167).
 */
class DiaryLinkSpan(
    private val link: String,
) : ClickableSpan() {
    override fun onClick(widget: View) {
        val context = widget.context
        when (val target = ClickableLinkUtils.resolveTarget(link)) {
            is DiaryLinkTarget.Diary -> context.startDiaryReadingActivity(target.sequence)
            is DiaryLinkTarget.Phone -> context.dialPhoneNumber(target.number)
            is DiaryLinkTarget.Web -> context.openWebPage(target.url)
            is DiaryLinkTarget.External -> context.openWebPage(target.address)
            null -> Unit
        }
    }
}
