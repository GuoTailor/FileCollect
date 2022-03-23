package com.gyh.fileindex.api

import android.os.AsyncTask
import androidx.annotation.UiThread
import com.gyh.fileindex.bean.HybridFile
import com.gyh.fileindex.bean.TabInfo
import com.gyh.fileindex.util.ThreadManager
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


object TabInfoData {
    const val tag = "tag"
    const val apk = "apk"
    const val zip = "zip"
    const val word = "word"
    const val excel = "excel"
    const val pdf = "pdf"
    const val ppt = "ppt"
    private val listeners = LinkedList<Monitor>()
    var data: List<TabInfo> = emptyList()
    var fileScan: FileScan? = null
    private val allSuffix: Array<String> by lazy {
        val suffixs = ArrayList<String>()
        data.map { suffixs.addAll(it.suffix) }
        suffixs.toTypedArray()
    }

    fun addListener(monitor: Monitor) {
        listeners.add(monitor)
    }

    fun removeListener(monitor: Monitor) {
        listeners.remove(monitor)
    }

    @UiThread
    fun scan(): Status {
        if (fileScan == null) {
            fileScan = FileScan(::updateProgress, ::updateResult)
        }
        when (fileScan?.status) {
            AsyncTask.Status.RUNNING -> return Status.RUNNING
            AsyncTask.Status.PENDING -> {
                clean()
                fileScan?.executeOnExecutor(ThreadManager.getInstance().executorService, *allSuffix)
            }
            AsyncTask.Status.FINISHED -> {
                fileScan = FileScan(::updateProgress, ::updateResult)
                clean()
                fileScan?.executeOnExecutor(ThreadManager.getInstance().executorService, *allSuffix)
            }
            else -> {}
        }
        return Status.OK
    }

    fun getTabInfo(tag: String): TabInfo {
        return data.find { it.text == tag } ?: throw IllegalStateException(tag + "不存在")
    }

    fun clean() {
        for (tabInfo in data) {
            tabInfo.fileInfos.clear()
            tabInfo.count = 0
        }
    }

    private fun updateProgress(files: Array<out HybridFile>) {
        operation(files[0])
        listeners.forEach {
            for (file in files) {
                if (it.isCare(file)) {
                    it.updateProgress(file)
                }
            }
        }
    }

    private fun updateResult(result: String) {
        listeners.forEach {
            it.updateResult(result)
        }
    }

    private fun operation(file: HybridFile) {
        data.forEach {
            if (it.exitSuffix(file.name() ?: "")) {
                it.addFileInfo(file)
            }
        }
    }

    /**
     * 关闭文件扫描
     */
    fun shutdown() = fileScan?.shutdown()

    enum class Status {
        /**
         * Indicates that the task is running.
         */
        RUNNING,

        /**
         * Indicates that [AsyncTask.onPostExecute] has finished.
         */
        OK
    }

}