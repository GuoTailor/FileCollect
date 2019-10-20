package com.gyh.fileindex.api

import java.io.File

interface Monitor {

    fun updateProgress(vararg files: File)

    fun updateResult(result: String)

    fun isCare(file: File): Boolean
}