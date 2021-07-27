package com.gyh.fileindex.api

import android.os.*
import com.gyh.fileindex.util.ThreadManager
import java.util.concurrent.Future

open class AsynchronizedTask<T>(
    /** 更新进步 */
    private val updateProgress: ((Array<out T>) -> Unit)? = null,
    /** 处理结果 */
    private val updateResult: ((String) -> Unit)? = null,
    /** 后台运行的操作 */
    val operation: AsynchronizedTask<T>.(Array<out String?>) -> Unit
) {
    private val handler = Handler(Looper.getMainLooper())
    private var future: Future<Unit>? = null

    fun doInBackground(vararg params: String?): String {
        operation(this, params)
        return "ok"
    }

    fun onProgressUpdate(vararg values: T) {
        updateProgress?.invoke(values)
    }

    fun onPostExecute(result: String) {
        updateResult?.invoke(result)
    }

    fun updateProgress(vararg values: T) = publishProgress(*values)

    fun publishProgress(vararg values: T) {
        handler.post {
            onProgressUpdate(*values)
        }
    }

    fun execute(vararg params: String?) {
        future = ThreadManager.getInstance().submit {
            val result = doInBackground(*params)
            handler.post { onPostExecute(result) }
        }
    }

    fun cancel(mayInterruptIfRunning: Boolean) {
        future?.cancel(mayInterruptIfRunning)
    }
}