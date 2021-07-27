package com.gyh.fileindex.bean

import android.graphics.drawable.Drawable
import java.io.File
import java.util.*

class TabInfo(
    val suffix: Array<String>,
    val fileInfos: MutableList<File>,
    var icon: Drawable? = null, //TODO 使用默认图标代替
    var count: Int = 0,
    var text: String
) {

    override fun hashCode(): Int {
        var result = suffix.contentHashCode()
        result = 31 * result + (icon?.hashCode() ?: 0)
        result = 31 * result + count.hashCode()
        result = 31 * result + text.hashCode()
        return result
    }

    fun exitSuffix(name: String) = this.suffix.find { name.lowercase(Locale.ROOT).endsWith(it) } != null


    fun addFileInfo(file: File) : File{
        fileInfos.add(file)
        count++
        return file
    }

    fun clear() {
        fileInfos.clear()
        count = 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TabInfo) return false

        if (!suffix.contentEquals(other.suffix)) return false
        if (fileInfos != other.fileInfos) return false
        if (icon != other.icon) return false
        if (count != other.count) return false
        if (text != other.text) return false

        return true
    }

}
