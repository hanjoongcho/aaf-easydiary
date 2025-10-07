package me.blog.korn123.commons.utils

import me.blog.korn123.easydiary.models.Diary


object TreeUtils {

    /**
     * Flattens the file tree into a list of pairs containing the node and its level in the tree.
     * The root node is excluded from the result.
     */
    fun flattenTree(node: FileNode, level: Int = 0, sortOption: String = "asc"): List<Pair<FileNode, Int>> {
        val list = mutableListOf<Pair<FileNode, Int>>()
        if (node.name != "root") list.add(node to level)
        when (sortOption) {
            "asc" -> node.children.sortedBy { it.fullPath }.forEach {
                list.addAll(flattenTree(it, level + 1, sortOption))
            }
            "desc" -> node.children.sortedByDescending { it.fullPath }.forEach {
                list.addAll(flattenTree(it, level + 1, sortOption))
            }
        }
        return list
    }

    /**
     * Builds a file tree structure from a list of paths.
     * Each path is expected to be in the format "dir1/dir2/file.txt".
     * If addOptionalTitle is true, the file name is added as the last part of the path.
     */
    fun buildFileTree(items: List<Diary>, addOptionalTitle: Boolean = false, partsGenerator: (diary: Diary) -> MutableList<String>): FileNode {
        val root = FileNode("root", sequence = 0)
        for (diary in items) {
            var current = root
//            val parts = "${diary.dateString}".split("-").toMutableList()
            val parts = partsGenerator(diary)
            if (addOptionalTitle) parts.add("${diary.currentTimeMillis.div(1000)}_${EasyDiaryUtils.summaryDiaryLabel(diary)}")
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

    fun toggleWholeTree(treeData: List<Pair<FileNode, Int>>, isExpand: Boolean): List<Pair<FileNode, Int>> {
        return treeData.map { data ->
            if (data.second == 1) {
                data.copy(first = data.first.copy(isFolderOpen = isExpand))
            } else if (data.first.isFile) {
                data.copy(first = data.first.copy(isShow = isExpand, isRootShow = isExpand))
            } else {
                data.copy(first = data.first.copy(isFolderOpen = isExpand, isShow = isExpand, isRootShow = isExpand))
            }
        }
    }

    fun toggleChildren(treeData: List<Pair<FileNode, Int>>, fileNode: FileNode): List<Pair<FileNode, Int>> {
        return treeData.map { data ->
            if (data.first.fullPath.startsWith(fileNode.fullPath) && data.first.fullPath != fileNode.fullPath) {
                val isFirstChildNode = fileNode.children.any {child -> child.fullPath == data.first.fullPath}
                if (isFirstChildNode) {
                    data.copy(first = data.first.copy(isShow = fileNode.isFolderOpen, isRootShow = isRootNodeVisible(treeData, data)))
                } else {
                    data.copy(first = data.first.copy(isRootShow = isRootNodeVisible(treeData, data)))
                }
            } else {
                data
            }
        }
    }

    fun isRootNodeVisible (treeData: List<Pair<FileNode, Int>>, data: Pair<FileNode, Int>): Boolean {
        if (data.second == 1) return true

        var isShow = false
        val parentNode = data.first.fullPath.split("/")
        var currentPath = ""
        for (i in 0 until parentNode.size.minus(1)) {
            currentPath += if (currentPath.isEmpty()) parentNode[i] else "/${parentNode[i]}"
            isShow = treeData.find { it -> it.first.fullPath == currentPath }?.first?.isFolderOpen ?: false
            if (!isShow) break
        }
        return isShow
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