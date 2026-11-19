package me.blog.korn123.easydiary.utils

import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class StringTranslationTest {

    @Test
    fun checkMissingTranslations() {
        val resDir = File("src/main/res")
        if (!resDir.exists()) {
            // If running from project root
            val rootResDir = File("app/src/main/res")
            if (rootResDir.exists()) {
                checkRes(rootResDir)
                return
            }
        }
        checkRes(resDir)
    }

    private fun checkRes(resDir: File) {
        val valuesDirs = resDir.listFiles { file -> file.isDirectory && file.name.startsWith("values") } ?: return
        valuesDirs.sortBy { it.name }

        val stringsData = mutableMapOf<String, Map<String, String>>()

        for (dir in valuesDirs) {
            val stringsFile = File(dir, "strings.xml")
            if (stringsFile.exists()) {
                val keys = mutableMapOf<String, String>()
                try {
                    val dbFactory = DocumentBuilderFactory.newInstance()
                    val dBuilder = dbFactory.newDocumentBuilder()
                    val doc = dBuilder.parse(stringsFile)
                    doc.documentElement.normalize()

                    val nodeList = doc.getElementsByTagName("string")
                    for (i in 0 until nodeList.length) {
                        val node = nodeList.item(i)
                        val name = node.attributes?.getNamedItem("name")?.nodeValue
                        if (name != null) {
                            keys[name] = node.textContent ?: ""
                        }
                    }
                    // Also check string-array and plurals if needed
                    val arrayList = doc.getElementsByTagName("string-array")
                    for (i in 0 until arrayList.length) {
                        val node = arrayList.item(i)
                        val name = node.attributes?.getNamedItem("name")?.nodeValue
                        if (name != null) {
                            keys[name] = ""
                        }
                    }
                    val pluralsList = doc.getElementsByTagName("plurals")
                    for (i in 0 until pluralsList.length) {
                        val node = pluralsList.item(i)
                        val name = node.attributes?.getNamedItem("name")?.nodeValue
                        if (name != null) {
                            keys[name] = ""
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                stringsData[dir.name] = keys
            }
        }

        val baseKeys = stringsData["values"]?.keys ?: emptySet()
        val baseKoKeys = stringsData["values-ko"]?.keys ?: emptySet()
        println("Base (values) has ${baseKeys.size} keys.")
        println("Base (values-ko) has ${baseKoKeys.size} keys.")

        val report = StringBuilder()
        val missingInKo = baseKeys - baseKoKeys
        val missingInValues = baseKoKeys - baseKeys
        if (missingInKo.isNotEmpty()) report.append("Missing in values-ko: $missingInKo\n")
        if (missingInValues.isNotEmpty()) report.append("Missing in values: $missingInValues\n")

        for ((dirName, keys) in stringsData) {
            if (dirName == "values" || dirName == "values-ko") continue
            val missing = baseKeys - keys.keys
            val extra = keys.keys - baseKeys
            if (missing.isNotEmpty()) {
                report.append("Locale $dirName: missing ${missing.size} keys: $missing\n")
            }
        }
        if (report.isNotEmpty()) {
            println(report.toString())
            org.junit.Assert.fail("Missing translations found:\n$report")
        }
    }
}
