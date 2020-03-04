package me.blog.korn123.commons.utils

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.junit.Assert
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

class PrepareReleaseTest {

    private fun determineLastReleaseStartLine(values: List<String>?): Int {
        var startIndex = -1
        values?.forEachIndexed { index, s ->
            if (s.contains("release_")) {
                val version = s.split("\"")[1].split("_")[1].toIntOrNull() ?: -1
//                println("$index: ${s.split("\"")[1]} version is $version")
                if (version > 0 && index > startIndex) {
                    startIndex = index
                }
            }
        }
        return startIndex
    }

    private fun determineLastReleaseEndLine(values: List<String>?, startIndex: Int, newReleaseLines: ArrayList<String>? = null): Int {
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

    @Test
    @Throws(Exception::class)
    fun determine_strings_xml() {
        var valuesDefault: List<String>? = null
        val newReleaseLines: ArrayList<String> = arrayListOf()
        File("./src/main/res/").listFiles()?.map { localeFolder ->
            if (localeFolder.name.startsWith("values")) {
                if (localeFolder.name == "values") {
                    valuesDefault = FileUtils.readLines(File(localeFolder.absolutePath + "/strings.xml"), StandardCharsets.UTF_8)
                    println("Total lines: ${valuesDefault?.size}")
                    val startIndex = determineLastReleaseStartLine(valuesDefault)
                    val endIndex = determineLastReleaseEndLine(valuesDefault, startIndex, newReleaseLines)
                    println("Current release: $startIndex ~ $endIndex")
                    println("============================================================")
                } else {
                    localeFolder.listFiles()?.map { targetFile ->
                        if (targetFile.name == "strings.xml") {
                            val valuesOther = FileUtils.readLines(targetFile, StandardCharsets.UTF_8)
                            println(localeFolder.name + ": " + valuesOther.size)
                            val valuesDefaultTotal = valuesDefault?.size ?: 0
                            val startIndex = determineLastReleaseStartLine(valuesOther)
                            val endIndex = determineLastReleaseEndLine(valuesOther, startIndex)
                            if (valuesOther.size < valuesDefaultTotal ) {
                                valuesOther.addAll(endIndex.plus(1), newReleaseLines)
                                val os = FileOutputStream(targetFile)
                                IOUtils.writeLines(valuesOther, null, os, StandardCharsets.UTF_8)
                                os.close()
                            } else if (valuesOther.size == valuesDefaultTotal) {
                                var num = 0
                                while (num < newReleaseLines.size) {
                                    valuesOther.removeAt(startIndex)
                                    num++
                                }
                                valuesOther.addAll(startIndex, newReleaseLines)
                                val os = FileOutputStream(targetFile)
                                IOUtils.writeLines(valuesOther, null, os, StandardCharsets.UTF_8)
                                os.close()
                            }
                        }
                    }
                }
            }
        }

        Assert.assertTrue(true)
    }
}