package com.gyh.fileindex.api

import android.os.AsyncTask
import android.os.Environment
import java.io.File
import java.util.*

class FileScan(
    private val updateProgress: ((Array<out File>) -> Unit)? = null,
    private val updateResult: ((String) -> Unit)? = null
) : AsyncTask<String, File, String>() {

    override fun doInBackground(vararg params: String): String {
        val file = Environment.getExternalStorageDirectory()
        scanFile(file, *params)
        return "完成"
    }

    override fun onProgressUpdate(vararg values: File) {
        updateProgress?.invoke(values)
    }

    override fun onPostExecute(result: String) {
        updateResult?.invoke(result)
    }

    private fun scanFile(file: File, vararg name: String) {
        if (file.isDirectory) {
            val fileList = file.listFiles()
            if (!fileList.isNullOrEmpty()) {
                for (subFile in fileList) {
                    scanFile(subFile, *name)
                }
            }
        }
        if (name.find { file.name.toLowerCase(Locale.ROOT).endsWith(it) } != null) {
            publishProgress(file)
        }
    }

}