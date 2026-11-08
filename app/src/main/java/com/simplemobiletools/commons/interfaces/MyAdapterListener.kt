package com.simplemobiletools.commons.interfaces

import java.util.*

interface MyAdapterListener {
    fun toggleItemSelectionAdapter(select: Boolean, position: Int)

    fun getSelectedPositions(): HashSet<Int>

    fun itemLongClicked(position: Int)
}
