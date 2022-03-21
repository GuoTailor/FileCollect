package com.gyh.fileindex.api

import com.gyh.fileindex.bean.HybridFile

interface Monitor {

    /**
     * 扫描的中间结果
     */
    fun updateProgress(vararg files: HybridFile)

    /**
     * 扫描结束后的操作
     */
    fun updateResult(result: String)

    /**
     * 关心的扫描结果，当放回true是才调用[updateProgress] 通知
     */
    fun isCare(file: HybridFile): Boolean
}