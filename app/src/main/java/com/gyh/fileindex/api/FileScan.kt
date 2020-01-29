package com.gyh.fileindex.api

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.gyh.fileindex.util.AppConfig
import eu.chainfire.libsuperuser.Shell
import java.io.File
import java.lang.StringBuilder
import java.util.*


class FileScan(
    private val updateProgress: ((Array<out File>) -> Unit)? = null,
    private val updateResult: ((String) -> Unit)? = null
) : AsyncTask<String, File, String>() {
    private var shellThreaded: Shell.Threaded = Shell.Builder().openThreaded()
    private var i = 0
    override fun doInBackground(vararg params: String): String {
        val file = Environment.getExternalStorageDirectory()
        val time = System.currentTimeMillis()
        scanFile(file.absolutePath, *params)
        //scanFile(file, *params)
        Log.d("TIME ", "" + (System.currentTimeMillis() - time))
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
            //Log.d("NMKA", file.toString())
            i++
        }
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
                    onPostExecute(line)
                }

                override fun onSTDOUT(line: String) {
                    //Log.d("NMKA2", line)
                    publishProgress(File(line))
                }

            })
        }
    }

    private fun nmka(context: Context): Int {
        /*val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)   //, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val path = Environment.getExternalStorageDirectory().toString() + "/AEXPapers/python介绍及后端说明.pptx"
        Log.d("NMKA", path)
        val uri = Uri.fromFile(File(path))
        intent.data = uri
        context.sendBroadcast(intent)*/

        val resolver = context.contentResolver

        //txt mime_type = text/plain
        val cursor = resolver.query(
            MediaStore.Files.getContentUri("external"),
            null,
            MediaStore.Files.FileColumns.DATA + " LIKE '%.mp4'",
            null,
            null
        )
        if (cursor != null) {
            var n = 0
            while (cursor.moveToNext()) {
                val title =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE))
                val path =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA))
                val fileLength =
                    cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE))
                val name =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME))
                //这里添加自己想要进行的操作可以了
                Log.d("NMKA", "$path $title $fileLength $name")
                n++
            }
            cursor.close()
            return n
        }



        return 0
    }


}