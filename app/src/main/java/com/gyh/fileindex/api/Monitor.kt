package com.gyh.fileindex.api

import java.io.File

interface Monitor {

    /**
     * 扫描的中间结果
     */
    fun updateProgress(vararg files: File)

    /**
     * 扫描结束后的操作
     */
    fun updateResult(result: String)

    /**
     * 关心的扫描结果，当放回true是才调用[updateProgress] 通知
     */
    fun isCare(file: File): Boolean
}