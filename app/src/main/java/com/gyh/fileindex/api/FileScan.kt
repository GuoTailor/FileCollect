package com.gyh.fileindex.api

import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.gyh.fileindex.bean.HybridFile
import com.gyh.fileindex.util.AppConfig
import com.gyh.fileindex.util.ThreadManager
import com.gyh.fileindex.util.Util
import eu.chainfire.libsuperuser.Shell
import java.io.File
import java.util.*


class FileScan(
    private val updateProgress: ((Array<out HybridFile>) -> Unit)? = null,
    private val updateResult: ((String) -> Unit)? = null
) : AsyncTask<String, HybridFile, String>() {
    private var shellThreaded: Shell.Threaded = Shell.Builder().openThreaded()
    override fun doInBackground(vararg params: String): String {
        val file = Environment.getExternalStorageDirectory()
        val time = System.currentTimeMillis()
        scanFile(file.absolutePath, *params)
        //scanFile(file, *params)
        Log.d("TIME ", "" + (System.currentTimeMillis() - time))
        return "完成 用时: ${System.currentTimeMillis() - time}ms"
    }

    override fun onProgressUpdate(vararg values: HybridFile) {
        updateProgress?.invoke(values)
    }

    override fun onPostExecute(result: String) {
        updateResult?.invoke(result)
    }

    private fun scanFile(path: String, vararg params: String) {
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
                    ThreadManager.getInstance().execute {
                        publishProgress(HybridFile(File(line)))
                    }
                }

            })
            DocumentFile.fromTreeUri(
                AppConfig.mInstance,
                Uri.parse(Util.changeToUri("/storage/emulated/0/Android/data"))
            )?.let {
                findFileByDocumentFile(it, *params, ".1")
            }
        }
    }

    private fun findFileByDocumentFile(documentFile: DocumentFile, vararg params: String) {
        if (documentFile.isDirectory) {
            for (listFile in documentFile.listFiles()) {
                if (listFile.uri.toString().startsWith("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fdata/document/primary%3AAndroid%2Fdata%2Fcom.tencent.mm"))
                    ThreadManager.getInstance().execute {
                        findFileByDocumentFile(listFile, *params)
                    }
            }
        } else {
            val name = documentFile.name!!
            val extension = Util.getExtension2(name)
            if (params.contains(extension, name)) {
                Log.i("FileScan", "findFileByDocumentFile: ${name} $extension")
                publishProgress(HybridFile(documentFile))
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
    }

}