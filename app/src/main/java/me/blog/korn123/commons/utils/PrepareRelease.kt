package me.blog.korn123.commons.utils

import android.annotation.SuppressLint
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

class PrepareRelease {
    companion object {
        const val SYNC_RELEASE_STRING = "sync_release_string"
        const val SYNC_NEW_STRING = "sync_new_string"
        const val SYNC_RELEASE_NOTE = "sync_release_note"
    }

    data class Release(var currentVersion: Int = -1, var currentVersionName: String = "", var currentVersionReleaseDate: String = "", var releaseInfoLines: ArrayList<String> = arrayListOf(), var startIndex: Int = -1, var endIndex: Int = -1)

    fun determineLastReleaseStartLine(values: List<String>?): Release {
        val defaultRelease = Release()
        var versionNameInfoIndex = -1
        values?.let { lines ->
            lines.forEachIndexed { index, s ->
                if (s.contains("release_")) {
                    val version = s.split("\"")[1].split("_")[1].toIntOrNull() ?: -1
//                println("$index: ${s.split("\"")[1]} version is $version")
                    if (version > 0 && index > defaultRelease.startIndex) {
                        defaultRelease.startIndex = index
                    }

                    if (version > defaultRelease.currentVersion) {
                        defaultRelease.currentVersion = version
                        versionNameInfoIndex = index.plus(1)
                    }
                }
            }
            defaultRelease.currentVersionName = lines[versionNameInfoIndex].split("/")[0].trim().replace("v", "")
            defaultRelease.currentVersionReleaseDate = lines[versionNameInfoIndex].split("/")[1].trim().replace("\\n", "")
        }
        return defaultRelease
    }

    fun determineLastReleaseEndLine(values: List<String>?, release: Release, updateInfoLines: Boolean = false): Int {
        var endIndex = -1
        values?.let {
            for (lineNum in release.startIndex until it.size) {
                if (it[lineNum].contains("</string>")) {
                    endIndex = lineNum
                    break
                }
            }

            if (updateInfoLines) {
                for (lineNum in release.startIndex until endIndex.plus(1)) {
                    release.releaseInfoLines.add(it[lineNum])
                }
            }
        }
        return endIndex;
    }

    @SuppressLint("NewApi")
    fun syncReleaseInformation(syncMode: String) {
        var valuesDefault: List<String>? = null
        var defaultRelease: Release? = null
        var defaultReleaseCurrentVersion = -1
        when (syncMode) {
            SYNC_RELEASE_STRING, SYNC_NEW_STRING -> {
                File("./app/src/main/res/").listFiles()?.map { localeFolder ->
                    if (localeFolder.name.startsWith("values")) {
                        if (localeFolder.name == "values") {
                            valuesDefault = FileUtils.readLines(File(localeFolder.absolutePath + "/strings.xml"), StandardCharsets.UTF_8)
                            println("Total lines: ${valuesDefault?.size}")
                            defaultRelease = determineLastReleaseStartLine(valuesDefault)
                            defaultRelease?.let {
                                println("======================================> Start: Current release info")
                                defaultReleaseCurrentVersion = it.currentVersion
                                it.endIndex = determineLastReleaseEndLine(valuesDefault, it, true)
                                println("Range of lines: ${it.startIndex} ~ ${it.endIndex}")
                                it.releaseInfoLines.map { line ->
                                    println(line)
                                }
                                println("Current version: $defaultReleaseCurrentVersion")
                                println("Current versionName: ${it.currentVersionName}")
                                println("======================================> End: Current release info")
                            }
                        } else {
                            localeFolder.listFiles()?.map { targetFile ->
                                if (targetFile.name == "strings.xml") {
                                    val valuesOther = FileUtils.readLines(targetFile, StandardCharsets.UTF_8)
                                    println(localeFolder.name + ": " + valuesOther.size)
                                    val valuesDefaultTotal = valuesDefault?.size ?: 0
                                    val otherRelease = determineLastReleaseStartLine(valuesOther)
                                    otherRelease.endIndex = determineLastReleaseEndLine(valuesOther, otherRelease)

                                    when {
                                        syncMode == SYNC_NEW_STRING -> {
                                            for (i in valuesOther.size until valuesDefaultTotal) {
                                                valuesOther.add(valuesOther.lastIndex, valuesDefault?.get(i.minus(1)))
                                            }
                                        }
                                        defaultReleaseCurrentVersion > otherRelease.currentVersion -> {
                                            defaultRelease?.let {
                                                valuesOther.addAll(otherRelease.endIndex.plus(1), it.releaseInfoLines)
                                            }
                                        }
                                        defaultReleaseCurrentVersion == otherRelease.currentVersion -> {
                                            println("remove line : ${otherRelease.startIndex} ~ ${otherRelease.endIndex}")
                                            for (removeLineNum in otherRelease.startIndex..otherRelease.endIndex) {
                                                valuesOther.removeAt(otherRelease.startIndex)
                                            }
                                            defaultRelease?.let {
                                                valuesOther.addAll(otherRelease.startIndex, it.releaseInfoLines)
                                            }
                                        }
                                    }
                                    val os = FileOutputStream(targetFile)
                                    IOUtils.writeLines(valuesOther, null, os, StandardCharsets.UTF_8)
                                    os.close()
                                }
                            }
                        }
                    }
                }
            }
            SYNC_RELEASE_NOTE -> {
                println("======================================> Start: sync release note")
                File("./app/src/main/assets/").listFiles()?.map { file ->
                    println(file.absolutePath)
                    if (file.name.startsWith("RELEASE")) {
                        val locale = when {
                            file.name.startsWith("RELEASE_en") -> "en"
                            file.name.startsWith("RELEASE_ja") -> "ja"
                            else -> "ko"
                        }
                        val releaseNotes = FileUtils.readLines(file, StandardCharsets.UTF_8)
                        releaseNotes?.let {
                            val infoLine = it[6]
                            val releaseNoteVersionName = infoLine.split("# Changes in ")[1].split(" ")[0]
                            val valuesFilePath = "./app/src/main/res/values-$locale/strings.xml"
                            val strings = FileUtils.readLines(File(valuesFilePath), StandardCharsets.UTF_8)
                            val release = determineLastReleaseStartLine(strings)
                            determineLastReleaseEndLine(strings, release, true)
                            if (releaseNoteVersionName != release.currentVersionName) {
                                println("Update release note version to ${release.currentVersionName} from $releaseNoteVersionName")
                                release.let { it ->
                                    println(it.currentVersionName)
                                    val newLines = arrayListOf("\n# Changes in ${release.currentVersionName} (date: ${release.currentVersionReleaseDate})")
                                    for (lineIndex in 2..it.releaseInfoLines.size.minus(2)) {
                                        val newLine = "  * ${it.releaseInfoLines[lineIndex].replace("\\n", "").trim()}"
                                        println(newLine)
                                        newLines.add(newLine)
                                    }
                                    releaseNotes.addAll(5, newLines)
                                }
                            }

                            val os = FileOutputStream(file)
                            IOUtils.writeLines(releaseNotes, null, os, StandardCharsets.UTF_8)
                            os.close()
                        }
                    }
                }
                println("======================================> End: sync release note")
            }
        }
    }
}

fun main() {
    val prepareRelease = PrepareRelease()
//    prepareRelease.syncReleaseInformation(PrepareRelease.SYNC_RELEASE_STRING)
//    prepareRelease.syncReleaseInformation(PrepareRelease.SYNC_NEW_STRING)
    prepareRelease.syncReleaseInformation(PrepareRelease.SYNC_RELEASE_NOTE)
}