package me.blog.korn123.commons.utils

import org.junit.Assert
import org.junit.Test
import java.io.File

class PrepareReleaseTest {
    @Test
    @Throws(Exception::class)
    fun determine_strings_xml() {
        File("./src/main/res/").listFiles().map {
            if (it.name.startsWith("values")) {
                it.listFiles().map { targetFile ->
                    if (targetFile.name == "strings.xml") {
                        println(targetFile.absolutePath)
                    }
                }
            }
        }

        Assert.assertTrue(true)
    }
}