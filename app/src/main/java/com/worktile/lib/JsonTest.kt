package com.worktile.lib

import com.worktile.json.annotation.Deserializer
import com.worktile.json.operator.parse

class Test {
    var value1 = 2
    var value2: List<Any> = emptyList()
    var value3: Any? = null

    @Deserializer
    fun test() {
        parse {
            "123" / "000" / "aaa" > ::value3
            "456" .. {
                "678" > ::value2
            } > ::value1
            "aaa" bindList ::value2
        }
    }
}