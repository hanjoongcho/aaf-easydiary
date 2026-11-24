package me.blog.korn123.easydiary.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A tag a diary can carry (issue #270).
 *
 * [name] keeps the spelling the writer first entered and [normalizedName] is the key two names are
 * compared by, so `'Trip'` and `'trip'` cannot become two rows: the unique index is on
 * [normalizedName]. [me.blog.korn123.easydiary.domain.model.TagRules] is the only place that builds
 * either of them.
 */
@Entity(
    tableName = "tags",
    indices = [Index(value = ["normalizedName"], unique = true)],
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    var tagId: Long = 0L,
    var name: String = "",
    var normalizedName: String = "",
)
