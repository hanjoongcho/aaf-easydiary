package me.blog.korn123.commons.utils

import org.apache.commons.io.FileUtils
import org.junit.Assert
import org.junit.Test
import java.io.File
import java.nio.charset.StandardCharsets

class PrepareReleaseTest {
    @Test
    @Throws(Exception::class)
    fun determine_strings_xml() {
        var valuesDefault: List<String>?
        var startIndex = -1
        var endIndex = -1
        File("./src/main/res/").listFiles()?.map { localeFolder ->
            if (localeFolder.name.startsWith("values")) {
                if (localeFolder.name == "values") {
                    valuesDefault = FileUtils.readLines(File(localeFolder.absolutePath + "/strings.xml"), StandardCharsets.UTF_8)
                    println("Total lines: ${valuesDefault?.size}")

                    valuesDefault?.forEachIndexed { index, s ->
                        if (s.contains("release_")) {
                            val version = s.split("\"")[1].split("_")[1].toIntOrNull() ?: -1
                            println("$index: ${s.split("\"")[1]} version is $version")
                            if (version > 0 && index > startIndex) {
                                startIndex = index
                            }
                        }
                    }
                    valuesDefault?.let {
                        for (lineNum in startIndex until it.size) {
                            if (it[lineNum].contains("</string>")) {
                                endIndex = lineNum
                                break
                            }
                        }
                        println("Current release: $startIndex ~ $endIndex")

                        for (lineNum in startIndex until endIndex.plus(1)) {
                            println(it[lineNum])
                        }
                    }
                }

                localeFolder.listFiles()?.map { targetFile ->
                    if (targetFile.name == "strings.xml") {
                        val lines = FileUtils.readLines(targetFile, StandardCharsets.UTF_8)
                        println(targetFile.absolutePath + ": " + lines.size)
                        lines.forEach { line ->
//                            println(line)
                        }


                    }
                }
            }
        }

        Assert.assertTrue(true)
    }
}