package com.gyh.fileindex.api

import android.os.AsyncTask

open class AsynchronizedTask<T>(
    private val updateProgress: ((Array<out T>) -> Unit)? = null,
    private val updateResult: ((String) -> Unit)? = null,
    val operation: AsynchronizedTask<T>.(Array<out String?>) -> Unit
) : AsyncTask<String, T, String>() {

    override fun doInBackground(vararg params: String?): String {
        operation(this, params)
        return "ok"
    }

    override fun onProgressUpdate(vararg values: T) {
        updateProgress?.invoke(values)
    }

    override fun onPostExecute(result: String) {
        updateResult?.invoke(result)
    }

    fun updateProgress(vararg values: T){
        publishProgress(*values)
    }
}