package com.gyh.fileindex

import com.gyh.fileindex.util.ThreadManager
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val threadPool = ThreadManager.getInstance()
        for (i in 1..7) {
            threadPool.execute {
                println("nmka$i")
                Thread.sleep(10_000)
            }
            println("end$i")
        }
    }
}
