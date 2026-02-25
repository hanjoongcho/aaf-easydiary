package me.blog.korn123.commons.utils

import me.blog.korn123.easydiary.helper.TreeConstants
import me.blog.korn123.easydiary.models.Diary

object TreeUtils {
    /**
     * Flattens the file tree into a list of pairs containing the node and its level in the tree.
     * The root node is excluded from the result.
     */
    fun flattenTree(
        node: FileNode,
        level: Int = 0,
        sortOption: String = TreeConstants.SORT_OPTION_ASC,
    ): List<Pair<FileNode, Int>> {
        val list = mutableListOf<Pair<FileNode, Int>>()
        if (node.name != TreeConstants.ROOT_NODE) list.add(node to level)
        when (sortOption) {
            TreeConstants.SORT_OPTION_ASC -> {
                node.children.sortedBy { it.fullPath }.forEach {
                    list.addAll(flattenTree(it, level + 1, sortOption))
                }
            }

            TreeConstants.SORT_OPTION_DESC -> {
                node.children.sortedByDescending { it.fullPath }.forEach {
                    list.addAll(flattenTree(it, level + 1, sortOption))
                }
            }
        }
        return list
    }

    /**
     * Builds a file tree structure from a list of paths.
     * Each path is expected to be in the format "dir1/dir2/file.txt".
     * If addOptionalTitle is true, the file name is added as the last part of the path.
     */
    fun buildFileTree(
        items: List<Diary>,
        addOptionalTitle: Boolean = false,
        addOptionalSortPrefix: Boolean = false,
        partsGenerator: (diary: Diary) -> MutableList<String>,
    ): FileNode {
        val root = FileNode(name = TreeConstants.ROOT_NODE, sequence = 0, weather = 0)
        for (diary in items) {
            var current = root
//            val parts = "${diary.dateString}".split("-").toMutableList()
            val parts = partsGenerator(diary)
            if (addOptionalTitle) parts.add(EasyDiaryUtils.summaryDiaryLabel(diary))
            var partPath = ""
            for ((i, part) in parts.withIndex()) {
                val isFile = i == parts.lastIndex
                partPath +=
                    if (partPath.isEmpty()) {
                        part
                    } else if (isFile && addOptionalSortPrefix) {
                        "/${diary.currentTimeMillis.div(1000)}_$part"
                    } else {
                        "/$part"
                    }
                val existing = current.children.find { it.name == part }
                if (existing != null) {
                    current = existing
                } else {
                    val newNode =
                        FileNode(
                            name = part,
                            fullPath = partPath,
                            isFile = isFile,
                            sequence = diary.sequence,
                            weather = diary.weather,
                            currentTimeMillis = diary.currentTimeMillis,
                        )
                    current.children.add(newNode)
                    current = newNode
                }
            }
        }
        return root
    }

    fun toggleWholeTree(
        treeData: List<Pair<FileNode, Int>>,
        isExpand: Boolean,
    ): List<Pair<FileNode, Int>> =
        treeData.map { pair ->
            if (pair.second == TreeConstants.LEVEL_START) {
                pair.copy(first = pair.first.copy(isFolderOpen = isExpand))
            } else if (pair.first.isFile) {
                pair.copy(first = pair.first.copy(isShow = isExpand, isParentFolderOpen = isExpand))
            } else {
                pair.copy(first = pair.first.copy(isFolderOpen = isExpand, isShow = isExpand, isParentFolderOpen = isExpand))
            }
        }

    fun toggleChildren(
        treeData: List<Pair<FileNode, Int>>,
        selectedNode: FileNode,
    ): List<Pair<FileNode, Int>> =
        treeData.map { pair ->
            val isSelectedFolderOpen = selectedNode.isFolderOpen.not()

            if (pair.first.fullPath == selectedNode.fullPath) {
                // 자기자신인 경우
                pair.copy(first = pair.first.copy(isFolderOpen = isSelectedFolderOpen))
            } else if (pair.first.fullPath.startsWith(selectedNode.fullPath) && pair.first.fullPath != selectedNode.fullPath) {
                // 자식 노드인 경우
                val isFirstChildNode = selectedNode.children.any { child -> child.fullPath == pair.first.fullPath }
                if (isFirstChildNode) {
                    // 선택노드 하위 1레벨 자식인 경우 선택노드 오픈여부에 따라 isShow 처리
                    pair.copy(first = pair.first.copy(isShow = isSelectedFolderOpen, isParentFolderOpen = isSelectedFolderOpen))
                } else {
                    pair.copy(first = pair.first.copy(isParentFolderOpen = isSelectedFolderOpen))
                }
            } else {
                pair
            }
        }

    @Deprecated(message = "Deprecated")
    fun isRootNodeVisible(
        treeData: List<Pair<FileNode, Int>>,
        pair: Pair<FileNode, Int>,
    ): Boolean {
        if (pair.second == TreeConstants.LEVEL_START) return true

        var isShow = false
        val parentNode = pair.first.fullPath.split("/")
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
    val currentTimeMillis: Long = 0,
    val sequence: Int,
    val weather: Int,
    var fullPath: String = "",
    var isShow: Boolean = true, // 현재 보이는 지 여부
    var isFolderOpen: Boolean = true, // 자기 자신이 폴더 일때 열려 있는지 여부
    var isParentFolderOpen: Boolean = true, // 상위 폴더가 열려 있는지 여부
)
