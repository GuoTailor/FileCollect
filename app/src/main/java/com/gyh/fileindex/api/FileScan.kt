package com.gyh.fileindex.api

import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.gyh.fileindex.bean.HybridFile
import com.gyh.fileindex.util.AppConfig
import com.gyh.fileindex.util.ThreadManager
import com.gyh.fileindex.util.Util
import eu.chainfire.libsuperuser.Shell
import java.io.File


class FileScan(
    updateProgress: ((Array<out HybridFile>) -> Unit)? = null,
    updateResult: ((String) -> Unit)? = null
) {
    private var converting: AsynchTask<HybridFile> =
        AsynchTask(
            updateProgress,
            updateResult,
            { this@FileScan.doInBackground(this, *it) },
            ThreadManager.getWorkPool()
        )

    private var shellThreaded: Shell.Threaded = Shell.Builder().openThreaded()

    fun execute(vararg params: String) {
        converting.execute(*params)
    }

    fun status() = converting.status

    private fun doInBackground(task: AsynchTask<HybridFile>, vararg params: String): String {
        val file = Environment.getExternalStorageDirectory()
        val time = System.currentTimeMillis()
        scanFile(file.absolutePath, task, *params)
        Log.d("TIME ", "" + (System.currentTimeMillis() - time))
        return "完成 用时: ${System.currentTimeMillis() - time}ms"
    }

    private fun scanFile(path: String, task: AsynchTask<HybridFile>, vararg params: String) {
        if (params.isNotEmpty()) {
            val cmds = StringBuilder("find $path -name ''")
            for (cmd in params) {
                cmds.append(" -o -iname '*$cmd'")
            }
            Log.d("scanFileOnShell", cmds.toString())
            shellThreaded.run(cmds.toString(), object : Shell.OnSyncCommandLineListener {
                override fun onSTDERR(line: String) {
                    Log.e("NMKA2-err", line)
                    //onPostExecute(line)
                }

                override fun onSTDOUT(line: String) {
                    ThreadManager.getWorkPool().execute {
                        task.publishProgress(HybridFile(File(line)))
                    }
                }

            })
            DocumentFile.fromTreeUri(
                AppConfig.mInstance,
                Uri.parse(Util.changeToUri("/storage/emulated/0/Android/data"))
            )?.let {
                findFileByDocumentFile(it, task, *params, ".1")
            }
        }
    }

    private fun findFileByDocumentFile(
        documentFile: DocumentFile,
        task: AsynchTask<HybridFile>,
        vararg params: String
    ) {
        if (documentFile.isDirectory) {
            for (listFile in documentFile.listFiles()) {
                if (listFile.uri.toString()
                        .startsWith("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3AAndroid%2Fdata%2Fcom.tencent.mm")
                )
                    ThreadManager.getWorkPool().execute {
                        findFileByDocumentFile(listFile, task, *params)
                    }
            }
        } else {
            val name = documentFile.name!!
            val extension = Util.getExtension2(name)
            if (params.contains(extension, name)) {
                Log.i("FileScan", "findFileByDocumentFile: $name $extension")
                task.publishProgress(HybridFile(documentFile))
            }
        }
    }

    fun Array<out String>.contains(element: String, name: String): Boolean {
        if (element.isEmpty()) return false
        if (element == ".1" && name.contains(".apk", true)) return true
        this.forEach {
            if (it == element) return true
        }
        return false
    }

    fun shutdown() {
        shellThreaded.close()
        converting.cancel(true)
    }
}