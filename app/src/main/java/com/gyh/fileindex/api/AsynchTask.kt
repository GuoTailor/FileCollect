package com.gyh.fileindex.api

import android.os.*
import com.gyh.fileindex.util.ThreadManager
import java.util.concurrent.Future

open class AsynchTask<T>(
    /** 更新进步 */
    private val updateProgress: ((Array<out T>) -> Unit)? = null,
    /** 处理结果 */
    private val updateResult: ((String) -> Unit)? = null,
    /** 后台运行的操作 */
    val operation: AsynchTask<T>.(Array<out String>) -> Unit,
    private val threadPool: ThreadManager = ThreadManager.getQuickPool()
) {
    private val handler = Handler(Looper.getMainLooper())
    private var future: Future<Unit>? = null

    @Volatile
    var status = Status.PENDING

    private fun doInBackground(vararg params: String): String {
        operation(this, params)
        return "ok"
    }

    private fun onProgressUpdate(vararg values: T) {
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

    fun execute(vararg params: String) {
        status = Status.RUNNING
        future = threadPool.submit {
            val result = doInBackground(*params)
            handler.post {
                onPostExecute(result)
                status = Status.OK
            }
        }
    }

    fun cancel(mayInterruptIfRunning: Boolean) {
        future?.cancel(mayInterruptIfRunning)
    }

    enum class Status {
        PENDING,

        /**
         * Indicates that the task is running.
         */
        RUNNING,

        /**
         * Indicates that [AsynchTask.onPostExecute] has finished.
         */
        OK
    }
}