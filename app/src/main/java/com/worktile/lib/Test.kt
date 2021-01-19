package com.worktile.lib

import com.worktile.json.annotation.Deserializer
import com.worktile.json.annotation.SerializedName
import com.worktile.json.parse

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


class Entry(
    @SerializedName("name") val nameA: String,
    val age: Int
) {

//    @SerializedName("name")
//    var nameA: String? = null
//
//    var age: Int? = null

    var weight: Double? = null

    var height: Long? = null

    var sex: Boolean? = null

    var address: String? = null

    var child: Entry? = null

    var peoples: List<People>? = null

    var children: List<ChildEntry>? = null

    @Deserializer
    fun toJson() {
        parse {
//            "name" > ::nameA
//            "age" > ::age
            "weight" > ::weight
            "height" > ::height
            "sex" > ::sex
            "address" > ::address
            "child" > ::child
            "children" > ::children
        }
    }
}

class ChildEntry {
    var name: String? = null

    var address: String? = null

    @Deserializer
    fun parser() {
        parse {
            "name" > ::name
            "address" > ::address
        }
    }
}