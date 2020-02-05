package me.blog.korn123.easydiary.helper

import android.util.Log
import org.apache.commons.io.IOUtils
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ZipHelper {
    private val fileNames = ArrayList<String>()
    private var rootDirectoryName: String? = null

    fun determineFiles(targetDirectoryName: String) {
        this.rootDirectoryName = targetDirectoryName
        determineFiles(targetDirectoryName, null)
    }

    fun determineFiles(targetDirectory: String, basePath: String?) {
        val sourceDir = File(targetDirectory)
        for (file in sourceDir.listFiles()) {
            if (file.isDirectory) {
                val currentBasePath = if (basePath != null) basePath + file.name + File.separator else file.name + File.separator
                determineFiles(file.absolutePath, currentBasePath)
            } else {
                if (basePath == null) {
                    fileNames.add(file.name)
                } else {
                    fileNames.add(basePath + file.name)
                }
            }
        }
    }

    fun compress(destFileName: String) {
        val zipOutputStream: ZipOutputStream
        try {
            zipOutputStream = ZipOutputStream(FileOutputStream(destFileName))

            fileNames.forEachIndexed { index, fileName ->
                Log.i("aaf", "$index/${fileNames.size}")
                try {
                    val fileInputStream = FileInputStream(rootDirectoryName + File.separator + fileName)
                    zipOutputStream.putNextEntry(ZipEntry(fileName))
                    val bytes = IOUtils.toByteArray(fileInputStream)
                    zipOutputStream.write(bytes, 0, bytes.size)
                    zipOutputStream.closeEntry()
                    fileInputStream.close()
                } catch (ex: IOException) {
                    System.err.println(ex)
                }
            }
            zipOutputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun decompress(zipFileName: String, compressDirectoryName: String) {
        val dir = File(compressDirectoryName)
        if (!dir.exists()) dir.mkdirs()

        val fileInputStream: FileInputStream
        val buffer = ByteArray(1024)
        try {
            fileInputStream = FileInputStream(zipFileName)
            val zipInputStream = ZipInputStream(fileInputStream)
            var zipEntry: ZipEntry? = zipInputStream.nextEntry
            while (zipEntry != null) {
                val fileName = zipEntry.name
                val newFile = File(compressDirectoryName + File.separator + fileName)
                System.out.println("Unzipping to " + newFile.absolutePath)

                File(newFile.parent).mkdirs()
                val fileOutputStream = FileOutputStream(newFile)
                var len: Int
                writeLoop@ while (true) {
                    len = zipInputStream.read(buffer)
                    if (len > 0) {
                        fileOutputStream.write(buffer, 0, len)
                    } else {
                        break@writeLoop
                    }
                }
                fileOutputStream.close()

                zipInputStream.closeEntry()
                zipEntry = zipInputStream.nextEntry
            }

            zipInputStream.closeEntry()
            zipInputStream.close()
            fileInputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun printFileNames() {
        for (path in fileNames) {
            println(path)
        }
    }
}