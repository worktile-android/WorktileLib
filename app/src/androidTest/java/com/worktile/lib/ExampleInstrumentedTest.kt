package com.worktile.lib

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.worktile.json.JsonDsl
import com.worktile.json.Parser
import com.worktile.json.ParserData
import org.json.JSONObject
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

    @Test
    fun testDirectReturn() {
        val json = JSONObject("{\n" +
                "        \"people\": {\n" +
                "            \"user\": {\n" +
                "                \"parent\": {\n" +
                "                    \"child\": {\n" +
                "                        \"name\": \"123\"\n" +
                "                    }\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }")
        Parser(ParserData(JsonDsl(), json)).apply {
            assertEquals("people.user.parent.child.name"(""), "123")
        }
    }


}
