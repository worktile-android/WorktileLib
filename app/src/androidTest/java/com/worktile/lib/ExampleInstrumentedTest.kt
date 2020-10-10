package com.worktile.lib

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.worktile.json.JsonDsl
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.worktile.lib", appContext.packageName)
    }

    @Test
    fun jsonParse() {
        val time = System.currentTimeMillis()
        val entry = JsonDsl().parse<Entry>("{\"name\":\"xixi\",\"age\":1,\"weight\":99.9,\"height\":101010101,\"sex\":true,\"address\":null}")
        println("DSL ${entry}")
    }
}
