package me.blog.korn123.commons.utils

import android.annotation.SuppressLint
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

class PrepareRelease {
    data class Release(var currentVersion: Int = -1, var startIndex: Int = -1, var endIndex: Int = -1)

    fun determineLastReleaseStartLine(values: List<String>?): Release {
        val defaultRelease = Release()
        values?.forEachIndexed { index, s ->
            if (s.contains("release_")) {
                val version = s.split("\"")[1].split("_")[1].toIntOrNull() ?: -1
//                println("$index: ${s.split("\"")[1]} version is $version")
                if (version > 0 && index > defaultRelease.startIndex) {
                    defaultRelease.startIndex = index
                }

                if (version > defaultRelease.currentVersion) defaultRelease.currentVersion = version
            }
        }
        return defaultRelease
    }

    fun determineLastReleaseEndLine(values: List<String>?, startIndex: Int, newReleaseLines: ArrayList<String>? = null): Int {
        var endIndex = -1
        values?.let {
            for (lineNum in startIndex until it.size) {
                if (it[lineNum].contains("</string>")) {
                    endIndex = lineNum
                    break
                }
            }

            for (lineNum in startIndex until endIndex.plus(1)) {
                println(it[lineNum])
                newReleaseLines?.add(it[lineNum])
            }
        }
        return endIndex;
    }

    @SuppressLint("NewApi")
    fun syncReleaseInformation() {
        var valuesDefault: List<String>? = null
        var defaultRelease: Release? = null
        val newReleaseLines: ArrayList<String> = arrayListOf()
        File("./app/src/main/res/").listFiles()?.map { localeFolder ->
            if (localeFolder.name.startsWith("values")) {
                if (localeFolder.name == "values") {
                    valuesDefault = FileUtils.readLines(File(localeFolder.absolutePath + "/strings.xml"), StandardCharsets.UTF_8)
                    println("Total lines: ${valuesDefault?.size}")
                    defaultRelease = determineLastReleaseStartLine(valuesDefault)
                    defaultRelease?.let {
                        it.endIndex = determineLastReleaseEndLine(valuesDefault, it.startIndex, newReleaseLines)
                        println("Current release: ${it.startIndex} ~ ${it.endIndex}")
                        println("============================================================")
                    }
                } else {
                    localeFolder.listFiles()?.map { targetFile ->
                        if (targetFile.name == "strings.xml") {
                            val valuesOther = FileUtils.readLines(targetFile, StandardCharsets.UTF_8)
                            println(localeFolder.name + ": " + valuesOther.size)
                            val valuesDefaultTotal = valuesDefault?.size ?: 0
                            val otherRelease = determineLastReleaseStartLine(valuesOther)
                            otherRelease.endIndex = determineLastReleaseEndLine(valuesOther, otherRelease.startIndex)
                            when {
                                (defaultRelease?.currentVersion ?: 0) > otherRelease.currentVersion -> {
                                    valuesOther.addAll(otherRelease.endIndex.plus(1), newReleaseLines)
                                }
                                valuesOther.size == valuesDefaultTotal -> {
                                    var num = 0
                                    while (num < newReleaseLines.size) {
                                        valuesOther.removeAt(otherRelease.startIndex)
                                        num++
                                    }
                                    valuesOther.addAll(otherRelease.startIndex, newReleaseLines)
                                }
                                else -> {
                                    for (i in valuesOther.size until valuesDefaultTotal) {
                                        valuesOther.add(valuesDefault?.get(i))
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
}

fun main() {
    val prepareRelease = PrepareRelease()
    prepareRelease.syncReleaseInformation()
}