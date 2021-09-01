package me.blog.korn123.commons.utils

import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper

class ChartUtils {
    companion object {
        fun getSortedMapBySymbol(isReverse: Boolean = false, startTimeMillis: Long = 0, endTimeMillis: Long = 0): Map<Int, Int> {
            val realmInstance = EasyDiaryDbHelper.getTemporaryInstance()
            val listDiary = EasyDiaryDbHelper.findDiary(null, false, startTimeMillis, endTimeMillis, realmInstance = realmInstance)
            realmInstance.close()
            val map = hashMapOf<Int, Int>()
            listDiary.map { diaryDto ->
                val targetColumn = diaryDto.weather
                if (targetColumn != 0) {
                    if (map[targetColumn] == null) {
                        map[targetColumn] = 1
                    } else {
                        map[targetColumn] = (map[targetColumn] ?: 0) + 1
                    }
                }
            }

            return when(isReverse) {
                true -> map.toList().sortedByDescending { (_, value) -> value }.toMap()
                false -> map.toList().sortedBy { (_, value) -> value }.toMap()
            } 
        }
    }
}