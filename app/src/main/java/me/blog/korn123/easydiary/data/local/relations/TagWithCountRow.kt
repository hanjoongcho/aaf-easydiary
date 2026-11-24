package me.blog.korn123.easydiary.data.local.relations

import androidx.room.Embedded
import me.blog.korn123.easydiary.data.local.entity.TagEntity

/**
 * A tag row together with the number of saved entries that carry it (issue #270), read by the tag manager.
 *
 * The count is taken with the tag in one query so the manager lists a tag and its usage without loading the
 * entries themselves.
 */
data class TagWithCountRow(
    @Embedded
    val tag: TagEntity,
    val diaryCount: Int,
)
