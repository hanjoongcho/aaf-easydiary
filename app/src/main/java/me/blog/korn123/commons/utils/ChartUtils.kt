package me.blog.korn123.commons.utils

import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper

class ChartUtils {
    companion object {
        fun getSortedMapBySymbol(): Map<Int, Int> {
            val listDiary = EasyDiaryDbHelper.readDiary(null)
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

            return map.toList().sortedByDescending { (_, value) -> value }.toMap()
        }
    }
}