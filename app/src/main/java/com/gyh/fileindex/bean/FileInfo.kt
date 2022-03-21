package com.gyh.fileindex.bean

import android.graphics.drawable.Drawable
import androidx.documentfile.provider.DocumentFile
import com.gyh.fileindex.util.Util
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

open class FileInfo(
    var name: String = "",
    var icon: Drawable? = null, //TODO 使用默认图标代替
    var path: String = "未知",
    var size: String = "未知",
    var intSize: Long = 0,
    var date: String = "未知",
    var file: HybridFile
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FileInfo) return false

        if (name != other.name) return false
        if (icon != other.icon) return false
        if (path != other.path) return false
        if (size != other.size) return false
        if (intSize != other.intSize) return false
        if (date != other.date) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (icon?.hashCode() ?: 0)
        result = 31 * result + path.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + intSize.hashCode()
        result = 31 * result + date.hashCode()
        return result
    }

    constructor(file: File) : this(
        date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(file.lastModified()),
        size = Util.getNetFileSizeDescription(file.length()),
        intSize = file.length(),
        path = file.absolutePath,
        name = file.name,
        file = HybridFile(file)
    )

    constructor(file: HybridFile) : this(
        date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(file.lastModified()),
        size = Util.getNetFileSizeDescription(file.length()),
        intSize = file.length(),
        path = file.absolutePath(),
        name = file.name() ?: "",
        file = file
    )
}
