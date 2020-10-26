package com.worktile.lib

import androidx.test.ext.junit.runners.AndroidJUnit4
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
    fun jsonParse() {
        val entry =
            JsonDsl().parse<Entry>("{\"name\":\"xixi\",\"age\":1,\"weight\":99.9,\"height\":101010101,\"sex\":true,\"address\":null,\"child\":{\"name\":\"xixi\",\"age\":1,\"weight\":99.9,\"height\":101010101,\"sex\":true,\"address\":null},\"peoples\":[{\"name\":\"呆\"}]}")
        assertEquals("xixi", entry.nameA)
        assertEquals(1, entry.age)
        assertEquals(99.9, entry.weight)
        assertEquals(101010101L, entry.height)
        assertEquals(true, entry.sex)
        assertEquals(null, entry.address)
        assertEquals("xixi", entry.child?.nameA)
        assertEquals("呆", entry.peoples?.get(0)?.name)
    }

    @Test
    fun checkoutDeserializer() {
        val entry =
            JsonDsl(false).parse<Entry>("{\"name\":\"xixi\",\"age\":1,\"weight\":99.9,\"height\":101010101,\"sex\":true,\"address\":null,\"child\":{\"name\":\"xixi\",\"age\":1,\"weight\":99.9,\"height\":101010101,\"sex\":true,\"address\":null},\"children\":[{\"name\":\"呆\"}]}")
        assertEquals("xixi", entry.nameA)
        assertEquals(1, entry.age)
        assertEquals(99.9, entry.weight)
        assertEquals(101010101L, entry.height)
        assertEquals(true, entry.sex)
        assertEquals(null, entry.address)
        assertEquals("xixi", entry.child?.nameA)
        assertEquals("呆", entry.children?.get(0)?.name)
    }

    @Test
    fun checkIgnoreOpt() {
        val entry =
            JsonDsl().parse<People>("{\"name\":\"xixi\",\"address\":\"Beijing\"}")
        assertEquals("xixi", entry.name)
        assertEquals(null, entry.address)
    }


}
