package me.blog.korn123.commons.utils

import me.blog.korn123.easydiary.models.Diary


object TreeUtils {

    /**
     * Flattens the file tree into a list of pairs containing the node and its level in the tree.
     * The root node is excluded from the result.
     */
    fun flattenTree(node: FileNode, level: Int = 0): List<Pair<FileNode, Int>> {
        val list = mutableListOf<Pair<FileNode, Int>>()
        if (node.name != "root") list.add(node to level)
        node.children.sortedByDescending { it.name }.forEach {
            list.addAll(flattenTree(it, level + 1))
        }
        return list
    }

    /**
     * Builds a file tree structure from a list of paths.
     * Each path is expected to be in the format "dir1/dir2/file.txt".
     */
    fun buildFileTree(items: List<Diary>, partsGenerator: (diary: Diary) -> MutableList<String>): FileNode {
        val root = FileNode("root", sequence = 0)
        for (diary in items) {
            var current = root
//            val parts = "${diary.dateString}".split("-").toMutableList()
            val parts = partsGenerator(diary)
            parts.add(EasyDiaryUtils.summaryDiaryLabel(diary))
            var partPath = ""
            for ((i, part) in parts.withIndex()) {
                partPath += if (partPath.isEmpty()) part else "/$part"
                val isFile = i == parts.lastIndex
                val existing = current.children.find { it.name == part }
                if (existing != null) {
                    current = existing
                } else {
                    val newNode = FileNode(
                        name = part,
                        fullPath = partPath,
                        isFile = isFile,
                        sequence = diary.sequence
                    )
                    current.children.add(newNode)
                    current = newNode
                }
            }
        }
        return root
    }
}

data class FileNode(
    val name: String,
    val children: MutableList<FileNode> = mutableListOf(),
    val isFile: Boolean = false,
    val sequence: Int,
    var fullPath: String = "",
    var isShow: Boolean = true,
    var isFolderOpen: Boolean = true,
    var isRootShow: Boolean = true,
)