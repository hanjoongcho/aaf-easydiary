package me.blog.korn123.easydiary.helper

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@RunWith(AndroidJUnit4::class)
class ZipHelperDecompressTest {
    private lateinit var context: Context
    private lateinit var workingRoot: File
    private lateinit var testId: String
    private val archives = mutableListOf<File>()
    private val createdLinks = mutableListOf<File>()
    private val outsideFiles = mutableListOf<File>()
    private val outsideDirectories = mutableListOf<File>()
    private val rootedAbsoluteDestinations = mutableListOf<File>()

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        testId = "ziphelper-containment-${UUID.randomUUID()}"
        workingRoot = File(Environment.getExternalStorageDirectory(), "EasyDiary")
        assertTrue("The configured EasyDiary working directory must be available to extraction", workingRoot.exists() || workingRoot.mkdirs())
    }

    @After
    fun tearDown() {
        createdLinks.forEach { it.delete() }

        if (::workingRoot.isInitialized && ::testId.isInitialized) {
            File(workingRoot, testId).deleteRecursively()
        }

        archives.forEach { it.delete() }
        rootedAbsoluteDestinations.forEach(::deleteFileAndEmptyParents)
        outsideFiles.forEach(::deleteFileAndEmptyParents)
        outsideDirectories.forEach { it.deleteRecursively() }
    }

    @Test
    fun decompressUri_extractsNestedRelativeEntryBeneathWorkingRoot() {
        val entryName = "$testId/nested/relative-entry.txt"
        val archive = createArchive(entryName, "contained content")

        ZipHelper(context).decompress(Uri.fromFile(archive))

        val extractedFile = File(workingRoot, entryName)
        assertTrue("A normal relative entry should be extracted", extractedFile.isFile)
        assertEquals("contained content", extractedFile.readText())
        assertTrue(
            "The extracted entry must remain below the canonical working-directory root",
            extractedFile.canonicalPath.startsWith(workingRoot.canonicalPath + File.separator)
        )
    }

    @Test
    fun decompressUri_rejectsTraversalEntryWithoutCreatingOutsideRootFile() {
        val outsideFile = File(workingRoot.parentFile, "$testId-traversal.txt")
        outsideFiles += outsideFile
        outsideFile.delete()
        val archive = createArchive("../${outsideFile.name}", "must not escape")

        ZipHelper(context).decompress(Uri.fromFile(archive))

        assertFalse("A traversal entry must not create a file outside the working root", outsideFile.exists())
    }

    @Test
    fun decompressUri_rejectsAbsoluteEntryWithoutCreatingDestination() {
        val outsideFile = File(context.cacheDir, "$testId-absolute.txt")
        outsideFiles += outsideFile
        outsideFile.delete()
        val absoluteEntryName = outsideFile.absolutePath
        val rootedAbsoluteDestination = File(
            workingRoot,
            absoluteEntryName.removePrefix(File.separator)
        )
        rootedAbsoluteDestinations += rootedAbsoluteDestination
        rootedAbsoluteDestination.delete()
        val archive = createArchive(absoluteEntryName, "must not be extracted")

        ZipHelper(context).decompress(Uri.fromFile(archive))

        assertFalse("An absolute ZIP entry must not create its absolute destination", outsideFile.exists())
        assertFalse(
            "An absolute ZIP entry must be rejected rather than treated as a relative destination",
            rootedAbsoluteDestination.exists()
        )
    }

    @Test
    fun decompressUri_rejectsEntryWhoseExistingParentLinkResolvesOutsideWorkingRoot() {
        Assume.assumeTrue(
            "Symbolic-link containment coverage requires Android O or later",
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        )

        val linkedParent = File(workingRoot, "$testId-linked-parent")
        val outsideDirectory = File(context.cacheDir, "$testId-link-target")
        createdLinks += linkedParent
        outsideDirectories += outsideDirectory
        linkedParent.delete()
        outsideDirectory.deleteRecursively()
        assertTrue("The link target directory must be created for this test", outsideDirectory.mkdirs())

        try {
            Files.createSymbolicLink(linkedParent.toPath(), outsideDirectory.toPath())
        } catch (exception: Exception) {
            Assume.assumeNoException(
                "The current Android test filesystem does not permit symbolic-link setup",
                exception
            )
            return
        }

        val escapedFile = File(outsideDirectory, "escaped-through-link.txt")
        outsideFiles += escapedFile
        val archive = createArchive("${linkedParent.name}/${escapedFile.name}", "must not follow link")

        ZipHelper(context).decompress(Uri.fromFile(archive))

        assertFalse(
            "A destination whose existing parent link resolves outside the working root must be rejected",
            escapedFile.exists()
        )
    }

    private fun createArchive(entryName: String, content: String): File {
        val archive = File(context.cacheDir, "$testId-${UUID.randomUUID()}.zip")
        archives += archive
        ZipOutputStream(FileOutputStream(archive)).use { output ->
            output.putNextEntry(ZipEntry(entryName))
            output.write(content.toByteArray())
            output.closeEntry()
        }
        return archive
    }

    private fun deleteFileAndEmptyParents(file: File) {
        file.delete()
        var parent = file.parentFile
        while (parent != null && parent != workingRoot) {
            if (!parent.delete()) {
                return
            }
            parent = parent.parentFile
        }
    }
}
