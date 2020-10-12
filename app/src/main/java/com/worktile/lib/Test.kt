package com.worktile.lib

import com.worktile.json.annotation.Deserializer
import com.worktile.json.operator.parse

/**
 * Created by Android Studio.
 * User: HuBo
 * Email: hubozzz@163.com
 * Date: 2020/10/10
 * Time: 3:09 PM
 * Desc:
 * {
 *  "name:'xi'
 * }
 */


class Entry {

    var name: Int? = null

    var age: Int? = null

    var weight: Double? = null

    var height: Long? = null

    var sex: Boolean? = null

    var address: String? = null

    @Deserializer
    fun toJson() {
        parse {
            "name" > ::name
            "age" > ::age
            "weight" > ::weight
            "height" > ::height
            "sex" > ::sex
            "address" > ::address
        }
    }
}