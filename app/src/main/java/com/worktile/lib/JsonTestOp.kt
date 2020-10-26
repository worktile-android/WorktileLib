package com.worktile.lib

import com.worktile.json.annotation.Deserializer
import com.worktile.json.operator.parse

class TestOp {
    var value1 = 2
    var value2: List<Any> = emptyList()
    var value3: Int? = null
    var value4: People? = null
    var value5: List<People> = emptyList()

    @Deserializer
    fun test() {
        parse {
            "123" / "000" / "aaa" > ::value3 {

            }
            "456" {
                "678" > ::value2 {

                }
            }
            "user" <People> {
                "name" > it::name
            } > ::value4
            "bbb" > ::value5
        }
    }
}