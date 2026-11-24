package me.blog.korn123.easydiary.domain.model

/**
 * A tag with the number of entries that carry it (issue #270), as the tag manager in the settings lists it.
 *
 * [diaryCount] counts only saved entries: a draft row of an editor is never shown in the main list, so a tag
 * that only a leftover draft carries counts as unused and the writer can delete it from the manager.
 */
data class TagWithCount(
    val tag: Tag,
    val diaryCount: Int,
)

/**
 * The outcome of adding a tag in the tag manager.
 *
 * A name is compared by [TagRules], so adding `'Trip'` when `'trip'` exists is [AlreadyExists] with the stored
 * tag instead of a second row.
 */
sealed interface TagCreateResult {
    data class Created(val tag: Tag) : TagCreateResult

    data class AlreadyExists(val tag: Tag) : TagCreateResult

    data object EmptyName : TagCreateResult
}

/**
 * The outcome of renaming a tag in the tag manager.
 *
 * [NameTaken] carries the tag that already holds the name: the manager asks the writer whether the two tags
 * should be merged, and merging is a separate step ([me.blog.korn123.easydiary.domain.repository.TagRepository.mergeTag]),
 * so a rename never removes entries of a tag by itself.
 */
sealed interface TagRenameResult {
    data class Renamed(val tag: Tag) : TagRenameResult

    data class NameTaken(val existing: Tag) : TagRenameResult

    /** The stored tag already has that name, so nothing was written. */
    data object Unchanged : TagRenameResult

    data object NotFound : TagRenameResult

    data object EmptyName : TagRenameResult
}
