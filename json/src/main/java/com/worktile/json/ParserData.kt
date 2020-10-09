package com.worktile.json

import org.json.JSONObject

data class ParserData(
    val jsonDsl: JsonDsl,
    val jsonObject: JSONObject,
    val keyInParent: String = "",
    val indexInArray: Int? = null
)