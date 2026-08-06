package me.blog.korn123.commons.utils

import me.blog.korn123.easydiary.helper.TreeConstants

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
                node.children
                    .sortedWith(
                        compareBy<FileNode> {
                            it.isFile // the priority is higher when the condition is false
                        }.thenBy { it.fullPath },
                    ).forEach {
                        list.addAll(flattenTree(it, level + 1, sortOption))
                    }
            }

            TreeConstants.SORT_OPTION_DESC -> {
                node.children
                    .sortedWith(
                        compareBy<FileNode> {
                            it.isFile // the priority is higher when the condition is false
                        }.thenByDescending { it.fullPath },
                    ).forEach {
                        list.addAll(flattenTree(it, level + 1, sortOption))
                    }
            }
        }
        return list
    }

    /**
     * Builds a file tree structure from a list of paths.
     */
    fun <T> buildFileTree(
        items: List<T>,
        addOptionalTitle: Boolean = false,
        addOptionalSortPrefix: Boolean = false,
        idGetter: (T) -> Int,
        symbolGetter: (T) -> Int,
        timeGetter: (T) -> Long,
        partsGenerator: (T) -> MutableList<String>,
        summaryLabelGenerator: (T) -> String = { "" },
    ): FileNode {
        val root = FileNode(name = TreeConstants.ROOT_NODE, sequence = 0, weather = 0)
        for (item in items) {
            var current = root
            val parts = partsGenerator(item)
            if (addOptionalTitle) parts.add(summaryLabelGenerator(item))
            var partPath = ""
            for ((i, part) in parts.withIndex()) {
                val isFile = i == parts.lastIndex
                partPath +=
                    if (partPath.isEmpty()) {
                        part
                    } else if (isFile && addOptionalSortPrefix) {
                        "/${timeGetter(item).div(1000)}_$part"
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
                            sequence = idGetter(item),
                            weather = symbolGetter(item),
                            currentTimeMillis = timeGetter(item),
                        )
                    current.children.add(newNode)
                    current = newNode
                }
            }
        }
        return root
    }

    fun buildFileTreeFromModels(
        items: List<me.blog.korn123.easydiary.models.Diary>,
        addOptionalTitle: Boolean = false,
        addOptionalSortPrefix: Boolean = false,
        partsGenerator: (me.blog.korn123.easydiary.models.Diary) -> MutableList<String>,
    ): FileNode =
        buildFileTree(
            items = items,
            addOptionalTitle = addOptionalTitle,
            addOptionalSortPrefix = addOptionalSortPrefix,
            idGetter = { it.sequence },
            symbolGetter = { it.weather },
            timeGetter = { it.currentTimeMillis },
            partsGenerator = partsGenerator,
            summaryLabelGenerator = { EasyDiaryUtils.summaryDiaryLabel(it) },
        )

    fun buildFileTreeFromDomain(
        items: List<me.blog.korn123.easydiary.domain.model.Diary>,
        addOptionalTitle: Boolean = false,
        addOptionalSortPrefix: Boolean = false,
        partsGenerator: (me.blog.korn123.easydiary.domain.model.Diary) -> MutableList<String>,
    ): FileNode =
        buildFileTree(
            items = items,
            addOptionalTitle = addOptionalTitle,
            addOptionalSortPrefix = addOptionalSortPrefix,
            idGetter = { it.diaryId },
            symbolGetter = { it.symbolSequence },
            timeGetter = { it.currentTimeMillis },
            partsGenerator = partsGenerator,
            summaryLabelGenerator = { diary ->
                if (diary.title.isNullOrEmpty()) diary.contents?.lines()?.firstOrNull() ?: "" else diary.title!!
            },
        )

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
            } else if (pair.first.fullPath.startsWith(selectedNode.fullPath + "/") && pair.first.fullPath != selectedNode.fullPath) {
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
