package me.blog.korn123.easydiary.adapters

import androidx.fragment.app.Fragment

class DotIndicatorPager2Adapter(
        fm: androidx.fragment.app.FragmentManager, private val fragmentList: ArrayList<Fragment>
) : androidx.fragment.app.FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getCount(): Int {
        return fragmentList.size
    }
}