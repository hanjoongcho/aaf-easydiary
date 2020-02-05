package me.blog.korn123.easydiary.helper

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.*
import java.io.File.separator
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream


@RequiresApi(Build.VERSION_CODES.O)
class ZipHelperWithVisitor : SimpleFileVisitor<Path> {

    private var sourceDir: Path
    private var zipOutputStream: ZipOutputStream

    constructor(sourceDir: Path, destZipFilePath: String) {
        this.sourceDir = sourceDir
        this.zipOutputStream = ZipOutputStream(FileOutputStream(destZipFilePath))
    }

    override fun visitFile(file: Path, attributes: BasicFileAttributes): FileVisitResult {
        try {
            val targetFile = sourceDir.relativize(file)
            zipOutputStream.putNextEntry(ZipEntry(targetFile.toString()))
            val bytes = Files.readAllBytes(file)
            zipOutputStream.write(bytes, 0, bytes.size)
            zipOutputStream.closeEntry()

        } catch (ex: IOException) {
            System.err.println(ex)
        }

        return FileVisitResult.CONTINUE
    }

    fun closeOutputStream() {
        try {
           zipOutputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun decompress(zipFilePath: String, destDir: String) {
        val dir = File(destDir)
        if (!dir.exists()) dir.mkdirs()

        val fileInputStream: FileInputStream
        val buffer = ByteArray(1024)
        try {
            fileInputStream = FileInputStream(zipFilePath)
            val zipInputStream = ZipInputStream(fileInputStream)
            var zipEntry = zipInputStream.nextEntry
            while (zipEntry != null) {
                val fileName = zipEntry.name
                val newFile = File(destDir + separator + fileName)

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
}