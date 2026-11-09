package com.simplemobiletools.commons.helpers

import android.content.ContentValues
import android.net.Uri
import com.simplemobiletools.commons.models.SharedTheme

class MyContentProvider {
    companion object {
        val AUTHORITY = "com.simplemobiletools.commons.provider"
        val CONTENT_URI = Uri.parse("content://$AUTHORITY/themes")
        val SHARED_THEME_ACTIVATED = "com.simplemobiletools.commons.SHARED_THEME_ACTIVATED"
        val SHARED_THEME_UPDATED = "com.simplemobiletools.commons.SHARED_THEME_UPDATED"

        val COL_ID = "_id"
        val COL_TEXT_COLOR = "text_color"
        val COL_BACKGROUND_COLOR = "background_color"
        val COL_PRIMARY_COLOR = "primary_color"
        val COL_LAST_UPDATED_TS = "last_updated_ts"

        fun fillThemeContentValues(sharedTheme: SharedTheme) = ContentValues().apply {
            put(COL_TEXT_COLOR, sharedTheme.textColor)
            put(COL_BACKGROUND_COLOR, sharedTheme.backgroundColor)
            put(COL_PRIMARY_COLOR, sharedTheme.primaryColor)
            put(COL_LAST_UPDATED_TS, System.currentTimeMillis() / 1000)
        }
    }
}
