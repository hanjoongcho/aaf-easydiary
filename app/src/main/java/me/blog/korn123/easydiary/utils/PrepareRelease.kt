package me.blog.korn123.easydiary.utils

import me.blog.korn123.easydiary.helper.PrepareReleaseConstants
import java.io.File
import java.nio.charset.StandardCharsets

class PrepareRelease {
    data class Release(
        var currentVersion: Int = -1,
        var currentVersionName: String = "",
        var currentVersionReleaseDate: String = "",
        var releaseInfoLines: ArrayList<String> = arrayListOf(),
        var startIndex: Int = -1,
        var endIndex: Int = -1,
    )

    fun determineLastReleaseStartLine(values: List<String>?): Release {
        val defaultRelease = Release()
        var versionNameInfoIndex = -1
        values?.let { lines ->
            lines.forEachIndexed { index, s ->
                if (s.contains("release_")) {
                    val version = s.split("\"")[1].split("_")[1].toIntOrNull() ?: -1
                    if (version > 0 && index > defaultRelease.startIndex) {
                        defaultRelease.startIndex = index
                    }

                    if (version > defaultRelease.currentVersion) {
                        defaultRelease.currentVersion = version
                        versionNameInfoIndex = index + 1
                    }
                }
            }
            if (versionNameInfoIndex in lines.indices) {
                val parts = lines[versionNameInfoIndex].split("/")
                if (parts.size >= 2) {
                    defaultRelease.currentVersionName = parts[0].trim().replace("v", "")
                    defaultRelease.currentVersionReleaseDate = parts[1].trim().replace("\\n", "")
                }
            }
        }
        return defaultRelease
    }

    fun determineLastReleaseEndLine(
        values: List<String>?,
        release: Release,
        updateInfoLines: Boolean = false,
    ): Int {
        var endIndex = -1
        values?.let {
            for (lineNum in release.startIndex until it.size) {
                if (it[lineNum].contains("</string>")) {
                    endIndex = lineNum
                    break
                }
            }

            if (updateInfoLines && release.startIndex in 0..endIndex) {
                for (lineNum in release.startIndex..endIndex) {
                    release.releaseInfoLines.add(it[lineNum])
                }
            }
        }
        return endIndex
    }

    fun syncReleaseInformation(syncMode: String) {
        val resDir = File("./src/main/res/")
        val valuesDir = File(resDir, "values")
        val defaultStringsFile = File(valuesDir, "strings.xml")

        if (!defaultStringsFile.exists()) {
            println("Default strings.xml not found at ${defaultStringsFile.absolutePath}")
            return
        }

        val valuesDefault = defaultStringsFile.readLines(StandardCharsets.UTF_8)
        println("Total lines in default strings.xml: ${valuesDefault.size}")

        val defaultRelease = determineLastReleaseStartLine(valuesDefault)
        val defaultReleaseCurrentVersion = defaultRelease.currentVersion
        defaultRelease.endIndex = determineLastReleaseEndLine(valuesDefault, defaultRelease, true)
        println("======================================> Start: Current release info")
        println("Range of lines: ${defaultRelease.startIndex} ~ ${defaultRelease.endIndex}")
        defaultRelease.releaseInfoLines.forEach { line -> println(line) }
        println("Current version: $defaultReleaseCurrentVersion")
        println("Current versionName: ${defaultRelease.currentVersionName}")
        println("======================================> End: Current release info")

        when (syncMode) {
            PrepareReleaseConstants.SYNC_RELEASE_STRING, PrepareReleaseConstants.SYNC_NEW_STRING -> {
                val otherLocaleFolders =
                    resDir.listFiles()?.filter {
                        it.isDirectory && it.name.startsWith("values-")
                    } ?: emptyList()

                otherLocaleFolders.forEach { localeFolder ->
                    val targetFile = File(localeFolder, "strings.xml")
                    if (targetFile.exists()) {
                        val valuesOther = targetFile.readLines(StandardCharsets.UTF_8).toMutableList()
                        println("${localeFolder.name}: ${valuesOther.size}")
                        val valuesDefaultTotal = valuesDefault.size
                        val otherRelease = determineLastReleaseStartLine(valuesOther)
                        otherRelease.endIndex = determineLastReleaseEndLine(valuesOther, otherRelease)

                        when (syncMode) {
                            PrepareReleaseConstants.SYNC_NEW_STRING -> {
                                for (i in valuesOther.size until valuesDefaultTotal) {
                                    valuesDefault.getOrNull(i - 1)?.let {
                                        valuesOther.add(valuesOther.lastIndex, it)
                                    }
                                }
                            }

                            PrepareReleaseConstants.SYNC_RELEASE_STRING -> {
                                if (defaultReleaseCurrentVersion > otherRelease.currentVersion) {
                                    valuesOther.addAll(otherRelease.endIndex + 1, defaultRelease.releaseInfoLines)
                                } else if (defaultReleaseCurrentVersion == otherRelease.currentVersion) {
                                    println("remove line : ${otherRelease.startIndex} ~ ${otherRelease.endIndex}")
                                    val linesToRemoveCount = (otherRelease.endIndex - otherRelease.startIndex + 1).coerceAtLeast(0)
                                    repeat(linesToRemoveCount) {
                                        if (otherRelease.startIndex in valuesOther.indices) {
                                            valuesOther.removeAt(otherRelease.startIndex)
                                        }
                                    }
                                    if (otherRelease.startIndex in 0..valuesOther.size) {
                                        valuesOther.addAll(otherRelease.startIndex, defaultRelease.releaseInfoLines)
                                    }
                                }
                            }
                        }

                        targetFile.printWriter(StandardCharsets.UTF_8).use { writer ->
                            valuesOther.forEach { writer.println(it) }
                        }
                    }
                }
            }

            PrepareReleaseConstants.SYNC_RELEASE_NOTE -> {
                println("======================================> Start: sync release note")
                val assetsDir = File("./src/main/assets/")
                assetsDir.listFiles()?.filter { it.isFile && it.name.startsWith("RELEASE") }?.forEach { file ->
                    println(file.absolutePath)
                    val locale =
                        when {
                            file.name.startsWith("RELEASE_en.txt") -> "en"
                            file.name.startsWith("RELEASE_ja.txt") -> "ja"
                            else -> "ko"
                        }
                    val releaseNotes = file.readLines(StandardCharsets.UTF_8).toMutableList()
                    if (releaseNotes.size > 6) {
                        val infoLine = releaseNotes[6]
                        if (infoLine.contains("# Changes in ")) {
                            val releaseNoteVersionName = infoLine.split("# Changes in ")[1].split(" ")[0]
                            val valuesFilePath = "./src/main/res/values-$locale/strings.xml"
                            val targetStringsFile = File(valuesFilePath)
                            if (targetStringsFile.exists()) {
                                val strings = targetStringsFile.readLines(StandardCharsets.UTF_8)
                                val release = determineLastReleaseStartLine(strings)
                                determineLastReleaseEndLine(strings, release, true)
                                if (releaseNoteVersionName != release.currentVersionName) {
                                    println("Update release note version to ${release.currentVersionName} from $releaseNoteVersionName")
                                    val newLines =
                                        arrayListOf(
                                            "\n# Changes in ${release.currentVersionName} (date: ${release.currentVersionReleaseDate})",
                                        )
                                    if (release.releaseInfoLines.size >= 3) {
                                        for (lineIndex in 2 until release.releaseInfoLines.size - 1) {
                                            val newLine = "  * ${release.releaseInfoLines[lineIndex].replace("\\n", "").trim()}"
                                            println(newLine)
                                            newLines.add(newLine)
                                        }
                                    }
                                    releaseNotes.addAll(5, newLines)
                                }

                                file.printWriter(StandardCharsets.UTF_8).use { writer ->
                                    releaseNotes.forEach { writer.println(it) }
                                }
                            }
                        }
                    }
                }
                println("======================================> End: sync release note")
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val prepareRelease = PrepareRelease()
            if (args.isNotEmpty()) {
                args.forEach { mode ->
                    prepareRelease.syncReleaseInformation(mode)
                }
            } else {
                prepareRelease.syncReleaseInformation(PrepareReleaseConstants.SYNC_RELEASE_STRING)
                prepareRelease.syncReleaseInformation(PrepareReleaseConstants.SYNC_NEW_STRING)
                prepareRelease.syncReleaseInformation(PrepareReleaseConstants.SYNC_RELEASE_NOTE)
            }
        }
    }
}
