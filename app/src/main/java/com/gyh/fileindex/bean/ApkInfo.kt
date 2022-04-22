package com.gyh.fileindex.bean

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.gyh.fileindex.R
import com.gyh.fileindex.util.AppConfig
import com.gyh.fileindex.util.Util
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ApkInfo(
    var appName: String = "未知",
    var packageName: String = "未知",
    var version: String = "未知",
    var currentVersion: String = "未安装",
    icon: Drawable? = null, //TODO 使用默认图标代替
    path: String = "未知",
    size: String = "未知",
    intSize: Long = 0,
    date: String = "未知",
    name: String = "未知",
    file: HybridFile
) : FileInfo(name, icon, path, size, intSize, date, file) {

    constructor(hybridFile: HybridFile) :this(
        date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(hybridFile.lastModified()),
        size = Util.getNetFileSizeDescription(hybridFile.length()),
        intSize = hybridFile.length(),
        path = hybridFile.absolutePath(),
        name = hybridFile.name() ?: "",
        file = hybridFile
    ) {
        if (hybridFile.type == HybridFile.FILE) {
            val file = hybridFile.file!!
            val pm = AppConfig.mInstance.packageManager
            val pkgInfo = pm.getPackageArchiveInfo(file.absolutePath, PackageManager.GET_ACTIVITIES)
            if (pkgInfo != null) {
                val appInfo = pkgInfo.applicationInfo
                /* 必须加这两句，不然下面icon获取是default icon而不是应用包的icon */
                appInfo.sourceDir = file.absolutePath
                appInfo.publicSourceDir = file.absolutePath

                appName = pm.getApplicationLabel(appInfo).toString()// 得到应用名
                packageName = appInfo.packageName // 得到包名
                version = pkgInfo.versionName ?: "未知" // 得到版本信息
                try {
                    currentVersion = "已安装：" +
                            pm.getPackageInfo(
                                appInfo.packageName,
                                PackageManager.GET_ACTIVITIES
                            ).versionName
                } catch (e: PackageManager.NameNotFoundException) {
                }
                /* icon1和icon2其实是一样的 */
                icon = pm.getApplicationIcon(appInfo)// 得到图标信息
                //val icon2 = appInfo.loadIcon(pm)
            }
            if (icon == null) {
                icon = ContextCompat.getDrawable(AppConfig.mInstance, R.drawable.ic_launcher_foreground)
            }
        } else {
            icon = ContextCompat.getDrawable(AppConfig.mInstance, R.drawable.ic_launcher_foreground)
            appName = name
            currentVersion = "未知"
        }
    }
}
