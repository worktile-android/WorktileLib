package com.worktile.lib

import com.worktile.json.annotation.Deserializer
import com.worktile.json.parse

class Test {
    var value1 = 2
    var value2: List<Any> = emptyList()
    var value3: Any? = null
    var value4: People? = null
    var value5: List<People> = emptyList()

    @Deserializer
    fun test() {
        parse {
            "123" / "000" / "aaa" > ::value3 {

            }
            "456" {
                "678" > ::value2
            } > ::value1
            "user" <People> {

            } > ::value4
            "ddd" > ::value5
            "ccc"[{

            }]
            "vvv"<String>()
        }
    }
}

class User {
    var name: String = ""
}