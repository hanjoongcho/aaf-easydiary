package me.blog.korn123.easydiary.data.local.dao

import me.blog.korn123.easydiary.data.local.entity.TagEntity

/**
 * What adding a tag in the tag manager changed in the `tags` table (issue #270).
 *
 * The write runs inside one transaction of [TagDao], so the outcome keeps the stored row: the repository maps it
 * to the domain result the screen acts on, and a name another tag already holds is reported with that tag
 * instead of replacing it.
 */
sealed interface TagCreateOutcome {
    /** The row was added with the name it carries. */
    data class Created(val tag: TagEntity) : TagCreateOutcome

    /** Another tag already holds the name; nothing was added. */
    data class Exists(val tag: TagEntity) : TagCreateOutcome

    /** The name holds no tag once [me.blog.korn123.easydiary.domain.model.TagRules] cleans it. */
    data object EmptyName : TagCreateOutcome
}

/**
 * What renaming a tag in the tag manager changed in the `tags` table (issue #270).
 *
 * The write runs inside one transaction of [TagDao]: the name another tag holds is reported with that tag so the
 * screen can ask the writer whether the two should be merged, and the merge itself stays a separate step.
 */
sealed interface TagRenameOutcome {
    /** The row now carries the new name. */
    data class Renamed(val tag: TagEntity) : TagRenameOutcome

    /** Another tag already holds the name; nothing was written. */
    data class NameTaken(val tag: TagEntity) : TagRenameOutcome

    /** The tag already has that name, so nothing was written. */
    data object Unchanged : TagRenameOutcome

    /** The tag is no longer stored, so nothing was written. */
    data object NotFound : TagRenameOutcome

    /** The name holds no tag once [me.blog.korn123.easydiary.domain.model.TagRules] cleans it. */
    data object EmptyName : TagRenameOutcome
}
