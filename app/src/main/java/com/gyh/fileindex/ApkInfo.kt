package com.gyh.fileindex

import android.graphics.drawable.Drawable

data class ApkInfo (
    var appName: String = "未知",
    var packageName: String = "未知",
    var version: String = "未知",
    var currentVersion: String = "未知",
    var icon: Drawable? = null, //TODO 使用默认图标代替
    var path: String = "未知",
    var size: String = "未知",
    var date: String = "未知"
)