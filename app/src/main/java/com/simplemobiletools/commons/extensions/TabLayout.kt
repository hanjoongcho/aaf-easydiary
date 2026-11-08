package com.simplemobiletools.commons.extensions

import com.google.android.material.tabs.TabLayout


fun TabLayout.onTabSelectionChanged(tabUnselectedAction: (inactiveTab: TabLayout.Tab) -> Unit, tabSelectedAction: (activeTab: TabLayout.Tab) -> Unit) =
        setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                tabSelectedAction(tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                tabUnselectedAction(tab)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })
