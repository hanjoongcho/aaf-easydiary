package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import androidx.viewpager.widget.ViewPager
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.models.DiarySymbol
import me.blog.korn123.easydiary.views.SlidingTabLayout

/**
 * The [android.support.v4.view.PagerAdapter] used to display pages in this sample.
 * The individual pages are simple and just display two lines of text. The important section of
 * this class is the [.getPageTitle] method which controls what is displayed in the
 * [SlidingTabLayout].
 */
class SymbolPagerAdapter(
        val activity: Activity,
        private val items: ArrayList<Array<String>>,
        private val categories: List<String>,
        private val selectedSymbolSequence: Int = 0,
        private val callback: (Int) -> Unit
) : androidx.viewpager.widget.PagerAdapter() {
    /**
     * @return the number of pages to display
     */
    override fun getCount(): Int {
        return items.size
    }

    /**
     * @return true if the value returned from [.instantiateItem] is the
     * same object as the [View] added to the [ViewPager].
     */
    override fun isViewFromObject(view: View, o: Any): Boolean {
        return o === view
    }

    // BEGIN_INCLUDE (pageradapter_getpagetitle)
    /**
     * Return the title of the item at `position`. This is important as what this method
     * returns is what is displayed in the [SlidingTabLayout].
     *
     *
     * Here we construct one using the position value, but for real application the title should
     * refer to the item's contents.
     */
    override fun getPageTitle(position: Int): CharSequence? {
        return categories[position]
    }
    // END_INCLUDE (pageradapter_getpagetitle)

    /**
     * Instantiate the [View] which should be displayed at `position`. Here we
     * inflate a layout from the apps resources and then change the text view to signify the position.
     */
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
//        selectedItemPosition = 0
        // Inflate a new layout from our resources
        val view = activity.layoutInflater.inflate(R.layout.dialog_feeling, container, false)
        // Add the newly created View to the ViewPager
        container.addView(view)

        val symbolList = arrayListOf<DiarySymbol>()
        var selectedItemPosition = 0
        items[position].map {item ->
            val diarySymbol = DiarySymbol(item)
            symbolList.add(diarySymbol)
            if (diarySymbol.sequence == selectedSymbolSequence) selectedItemPosition = symbolList.size.minus(1)
        }

        val arrayAdapter = DiaryWeatherItemAdapter(activity, R.layout.item_weather, symbolList)
        val gridView = view.findViewById<GridView>(R.id.feelingSymbols)
        Handler(Looper.getMainLooper()).post { gridView.adapter = arrayAdapter }
        if (selectedItemPosition > 0) {
            gridView.post { gridView.setSelection(selectedItemPosition) }
        }
        gridView.setOnItemClickListener { parent, view, position, id ->
            val diarySymbol = parent.adapter.getItem(position) as DiarySymbol
            callback.invoke(diarySymbol.sequence)
//            selectFeelingSymbol()
//            mDialog?.dismiss()
        }
        return view
    }

    /**
     * Destroy the item from the [ViewPager]. In our case this is simply removing the
     * [View].
     */
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}