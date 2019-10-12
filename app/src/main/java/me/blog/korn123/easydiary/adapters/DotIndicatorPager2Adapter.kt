package me.blog.korn123.easydiary.adapters

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import me.blog.korn123.easydiary.fragments.SettingsBasic
import me.blog.korn123.easydiary.fragments.SettingsLock

class DotIndicatorPager2Adapter(fm: androidx.fragment.app.FragmentManager, private val fragmentList: ArrayList<Fragment>) : androidx.fragment.app.FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return fragmentList.get(position)
    }

    override fun getCount(): Int {
        return fragmentList.size
    }
}