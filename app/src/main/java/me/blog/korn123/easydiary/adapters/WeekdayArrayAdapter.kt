package me.blog.korn123.easydiary.adapters

import android.content.Context
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.caldroid.R
import me.blog.korn123.commons.utils.FontUtils

class WeekdayArrayAdapter(context: Context, textViewResourceId: Int,
                               objects: List<String>, themeResource: Int) : com.roomorama.caldroid.WeekdayArrayAdapter(context, textViewResourceId, objects, themeResource) {

    val localInflater: LayoutInflater = getLayoutInflater(getContext(), themeResource);
    
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // To customize text size and color
        val textView = localInflater.inflate(R.layout.weekday_textview, null) as TextView

        // Set content
        val item = getItem(position)
        textView.text = item
        FontUtils.setFontsTypeface(context, context.assets, "", parent)

        return textView
    }

    private fun getLayoutInflater(context: Context, themeResource: Int): LayoutInflater {
        val wrapped = ContextThemeWrapper(context, themeResource)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return inflater.cloneInContext(wrapped)
    }
}