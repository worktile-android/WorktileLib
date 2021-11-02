package com.worktile.ui.recyclerview

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Test

import org.junit.Assert.*
import java.util.concurrent.Executors

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    private val diffThreadExecutor = Executors.newSingleThreadExecutor()

    @Test
    fun test() {
        runBlocking {
            run(0)
            run(1)
        }
    }

    private suspend fun run(index: Int) {
        println("run $index")
//        diffThreadExecutor.execute {
            println("execute $index: ${Thread.currentThread()}")
//            runBlocking {
                println("runBlocking $index: ${Thread.currentThread()}")
                delay(5000)
                withContext(Dispatchers.IO) {
                    println("onMain $index: ${Thread.currentThread()}")
                    delay(3000)
                }
                println("runBlockingEnd $index: ${Thread.currentThread()}")
//            }
            println("outBlocking $index: ${Thread.currentThread()}")
//        }
        println("outExecute $index: ${Thread.currentThread()}")
    }
}