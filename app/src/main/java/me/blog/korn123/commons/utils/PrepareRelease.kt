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

    data class Release(var currentVersion: Int = -1, var currentVersionName: String = "", var releaseInfoLines: ArrayList<String> = arrayListOf(), var startIndex: Int = -1, var endIndex: Int = -1)

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
        File("./app/src/main/res/").listFiles()?.map { localeFolder ->
            if (localeFolder.name.startsWith("values")) {
                if (localeFolder.name == "values") {
                    valuesDefault = FileUtils.readLines(File(localeFolder.absolutePath + "/strings.xml"), StandardCharsets.UTF_8)
                    println("Total lines: ${valuesDefault?.size}")
                    defaultRelease = determineLastReleaseStartLine(valuesDefault)
                    defaultRelease?.let {
                        defaultReleaseCurrentVersion = it.currentVersion
                        it.endIndex = determineLastReleaseEndLine(valuesDefault, it, true)
                        println("Range of lines: ${it.startIndex} ~ ${it.endIndex}")
                        it.releaseInfoLines.map { line ->
                            println(line)
                        }
                        println("Current version: $defaultReleaseCurrentVersion")
                        println("Current versionName: ${it.currentVersionName}")
                        println("============================================================")
                    }
                } else if (syncMode != SYNC_RELEASE_NOTE) {
                    localeFolder.listFiles()?.map { targetFile ->
                        if (targetFile.name == "strings.xml") {
                            val valuesOther = FileUtils.readLines(targetFile, StandardCharsets.UTF_8)
                            println(localeFolder.name + ": " + valuesOther.size)
                            val valuesDefaultTotal = valuesDefault?.size ?: 0
                            val otherRelease = determineLastReleaseStartLine(valuesOther)
                            otherRelease.endIndex = determineLastReleaseEndLine(valuesOther, otherRelease)

                            when {
                                syncMode == SYNC_RELEASE_STRING && defaultReleaseCurrentVersion > otherRelease.currentVersion -> {
                                    defaultRelease?.let {
                                        valuesOther.addAll(otherRelease.endIndex.plus(1), it.releaseInfoLines)
                                    }
                                }
                                syncMode == SYNC_RELEASE_STRING && defaultReleaseCurrentVersion == otherRelease.currentVersion -> {
                                    println("remove line : ${otherRelease.startIndex} ~ ${otherRelease.endIndex}")
                                    for (removeLineNum in otherRelease.startIndex..otherRelease.endIndex) {
                                        valuesOther.removeAt(otherRelease.startIndex)
                                    }
                                    defaultRelease?.let {
                                        valuesOther.addAll(otherRelease.startIndex, it.releaseInfoLines)
                                    }
                                }
                                syncMode == SYNC_NEW_STRING -> {
                                    for (i in valuesOther.size until valuesDefaultTotal) {
                                        valuesOther.add(valuesOther.lastIndex, valuesDefault?.get(i.minus(1)))
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

        if (syncMode == SYNC_RELEASE_NOTE) {

        }
    }
}

fun main() {
    val prepareRelease = PrepareRelease()
//    prepareRelease.syncReleaseInformation(PrepareRelease.SYNC_RELEASE_STRING)
//    prepareRelease.syncReleaseInformation(PrepareRelease.SYNC_NEW_STRING)
    prepareRelease.syncReleaseInformation(PrepareRelease.SYNC_RELEASE_NOTE)
}