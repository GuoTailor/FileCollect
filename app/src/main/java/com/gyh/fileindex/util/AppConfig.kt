package com.gyh.fileindex.util

import android.app.Application
import android.util.Log

class AppConfig : Application() {

    override fun onCreate() {
        super.onCreate()
        mInstance = this
        val poolSize = Util.getNumberOfCPUCores()
        Log.d("TAG", "" + Util.getNumberOfCPUCores())
        if (poolSize > 1)
            ThreadManager.init(poolSize)
    }

    companion object {
        lateinit var mInstance: AppConfig
    }
}
