package com.gyh.fileindex.api

import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import com.gyh.fileindex.util.ThreadManager
import eu.chainfire.libsuperuser.Shell
import java.io.File


class FileScan(
    private val updateProgress: ((Array<out File>) -> Unit)? = null,
    private val updateResult: ((String) -> Unit)? = null
) : AsyncTask<String, File, String>() {
    private var shellThreaded: Shell.Threaded = Shell.Builder().openThreaded()
    override fun doInBackground(vararg params: String): String {
        val file = Environment.getExternalStorageDirectory()
        val time = System.currentTimeMillis()
        scanFile(file.absolutePath, *params)
        //scanFile(file, *params)
        Log.d("TIME ", "" + (System.currentTimeMillis() - time))
        return "完成 用时: ${System.currentTimeMillis() - time}ms"
    }

    override fun onProgressUpdate(vararg values: File) {
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
                        publishProgress(File(line))
                    }
                }

            })
        }
    }

    fun shutdown() {
        shellThreaded.close()
    }

}