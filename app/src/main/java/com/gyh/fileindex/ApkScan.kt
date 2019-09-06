package com.gyh.fileindex

import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import com.gyh.fileindex.util.Util
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.SimpleFormatter

class ApkScan(
    private val updateProgress: (ApkInfo) -> Unit,
    private val updateResult: (String) -> Unit,
    private val context: Context
) : AsyncTask<String, ApkInfo, String>() {

    override fun doInBackground(vararg p0: String): String {
        val file = Environment.getExternalStorageDirectory()
        findFile(file, p0[0])
        return "ok:"
    }

    override fun onProgressUpdate(vararg values: ApkInfo) {
        updateProgress(values[0])
    }

    override fun onPostExecute(result: String) {
        updateResult(result)
    }

    private fun findFile(file: File, name: String) {
        if (file.isDirectory) {
            val fileList = file.listFiles()
            if (!fileList.isNullOrEmpty()) {
                for (subFile in fileList) {
                    findFile(subFile, name)
                }
            }
        }
        if (file.name.endsWith(name)) {
            publishProgress(apkInfo(file))
            //Log.d("ApkScan", file.absolutePath)
        }
    }


    /**
     * 获取apk包的信息：版本号，名称，图标等
     * @param file apk包的绝对路径
     * @param context
     */
    fun apkInfo(file: File): ApkInfo {
        val pm = context.packageManager
        val pkgInfo = pm.getPackageArchiveInfo(file.absolutePath, PackageManager.GET_ACTIVITIES)
        val apkInfo = ApkInfo(path = file.absolutePath)
        if (pkgInfo != null) {
            val appInfo = pkgInfo.applicationInfo
            /* 必须加这两句，不然下面icon获取是default icon而不是应用包的icon */
            appInfo.sourceDir = file.absolutePath
            appInfo.publicSourceDir = file.absolutePath

            apkInfo.appName = pm.getApplicationLabel(appInfo).toString()// 得到应用名
            apkInfo.packageName = appInfo.packageName // 得到包名
            apkInfo.version = pkgInfo.versionName // 得到版本信息
            try {
                apkInfo.currentVersion = "已安装：" +
                    pm.getPackageInfo(appInfo.packageName, PackageManager.GET_ACTIVITIES).versionName
            } catch (e: PackageManager.NameNotFoundException) { }
            /* icon1和icon2其实是一样的 */
            apkInfo.icon = pm.getApplicationIcon(appInfo)// 得到图标信息
            //val icon2 = appInfo.loadIcon(pm)
        }
        apkInfo.date =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(file.lastModified())
        apkInfo.size = Util.getNetFileSizeDescription(file.length())
        return apkInfo
    }

}