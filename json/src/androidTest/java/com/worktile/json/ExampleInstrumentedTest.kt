package com.worktile.json

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    data class Te(val a: String, val b: String) {
        var c: String? = null
    }

    class H

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.worktile.json.test", appContext.packageName)
    }

    @Test
    fun test() {
        println("1111")
        Te::class.declaredMemberProperties.forEach { property ->
            println(property)
        }
        println(Te::class.primaryConstructor?.parameters?.get(0)?.kind)
    }

    @Test
    fun h() {
        println(H::class.primaryConstructor)
    }
}