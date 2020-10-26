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
            "123" alter "000" alter "aaa" into ::value3 attach {

            }
            "456" then {
                "678" into ::value2
            } into ::value1
            "user".parse<People> {

            } into ::value4
            "ddd" into ::value5
        }
    }
}

class User {
    var name: String = ""
}