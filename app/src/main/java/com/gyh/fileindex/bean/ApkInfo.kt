package com.gyh.fileindex.bean

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
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
    file: File
) : FileInfo(name, icon, path, size, intSize, date, file) {

    constructor(file: File) : this(
        date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(file.lastModified()),
        size = Util.getNetFileSizeDescription(file.length()),
        intSize = file.length(),
        path = file.absolutePath,
        name = file.name,
        file = file
    ) {
        val pm = AppConfig.mInstance.packageManager
        val pkgInfo = pm.getPackageArchiveInfo(file.absolutePath, PackageManager.GET_ACTIVITIES)
        if (pkgInfo != null) {
            val appInfo = pkgInfo.applicationInfo
            /* 必须加这两句，不然下面icon获取是default icon而不是应用包的icon */
            appInfo.sourceDir = file.absolutePath
            appInfo.publicSourceDir = file.absolutePath

            appName = pm.getApplicationLabel(appInfo).toString()// 得到应用名
            packageName = appInfo.packageName // 得到包名
            version = pkgInfo.versionName // 得到版本信息
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
            icon = AppConfig.mInstance.getDrawable(R.drawable.ic_launcher_foreground)
        }
    }
}
