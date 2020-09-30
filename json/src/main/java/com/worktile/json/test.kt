package com.worktile.json

import com.worktile.json.annotation.Deserializer
import com.worktile.json.dsl.parse

class Test {
    var value1 = 2
    var value2: List<Any> = emptyList()
    var value3: Any? = null

    @Deserializer
    fun test() {
        parse {
            "123" alter "000" alter "aaa" into ::value3
            "456" then {
                "678" into ::value2
            } into ::value1
        }
    }
}