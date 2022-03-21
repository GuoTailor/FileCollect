package com.gyh.fileindex.bean

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import com.gyh.fileindex.util.Util
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream

class HybridFile {
    val type: Int
    var file: File? = null
    var documentFile: DocumentFile? = null

    constructor(file: File) {
        type = FILE
        this.file = file
    }

    constructor(documentFile: DocumentFile) {
        type = DOCUMENT_FILE
        this.documentFile = documentFile
    }

    companion object {
        const val FILE = 1
        const val DOCUMENT_FILE = 2
    }

    fun lastModified(): Long {
        return if (type == FILE) file!!.lastModified()
        else documentFile!!.lastModified()
    }

    fun length(): Long {
        return if (type == FILE) file!!.length()
        else documentFile!!.length()
    }

    fun absolutePath(): String {
        return if (type == FILE) file!!.absolutePath
        else Util.treeToPath(documentFile!!.uri.toString())
    }

    fun name(): String? {
        return if (type == FILE) file!!.name
        else documentFile?.name
    }

    fun exists(): Boolean {
        return if (type == FILE) file!!.exists()
        else documentFile!!.exists()
    }

    fun isDirectory(): Boolean {
        return if (type == FILE) file!!.isDirectory
        else documentFile!!.isDirectory
    }

    fun isFile(): Boolean {
        return if (type == FILE) file!!.isFile
        else documentFile!!.isFile
    }

    fun list(): List<String?> {
        return if (type == FILE) file!!.list()?.toList() ?: listOf()
        else documentFile!!.listFiles().map { it.name }
    }

    fun listFiles(): List<HybridFile> {
        return if (type == FILE) {
            val listFiles = file!!.listFiles()
            listFiles?.map { HybridFile(it) } ?: listOf()
        }
        else documentFile!!.listFiles().map { HybridFile(it) }
    }

    fun getOutputStream(context: Context): InputStream? {
        return if (type == FILE) {
            try {
                FileInputStream(file)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                null
            }
        } else {
            val contentResolver = context.contentResolver
            try {
                contentResolver.openInputStream(documentFile!!.uri)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                null
            }
        }
    }
}