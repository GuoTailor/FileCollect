package com.gyh.fileindex.util

import android.app.Application

class AppConfig : Application() {

    override fun onCreate() {
        super.onCreate()
        mInstance = this
    }

    companion object {
        lateinit var mInstance: AppConfig
    }
}
