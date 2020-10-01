package com.worktile.json.operator

import com.worktile.json.JsonDsl
import com.worktile.json.Parser
import com.worktile.json.ParserData
import java.lang.Exception
import kotlin.reflect.KMutableProperty0

fun Any.parse(block: ParserOperator.() -> Unit) {
    val parserData = JsonDsl.jsonMap[this]
    parserData?.run {
        block.invoke(ParserOperator(parserData))
    } ?: throw Exception("没有找到json, $this")
}

class ParserOperator(parserData: ParserData) : Parser(parserData) {
    inline infix operator fun <reified T> String.compareTo(property: KMutableProperty0<T>): Int {
        into(property)
        return 0
    }

    infix operator fun String.rangeTo(block: Parser.() -> Unit): ThenResult {
        return then(block)
    }

    infix operator fun String.div(key: String): AlterResult {
        return alter(key)
    }

    inline infix operator fun <reified T> ThenResult.compareTo(property: KMutableProperty0<T>): Int {
        into(property)
        return 0
    }

    infix operator fun AlterResult.div(key: String): AlterResult {
        return alter(key)
    }

    inline infix operator fun <reified T> AlterResult.compareTo(property: KMutableProperty0<T>): Int {
        into(property)
        return 0
    }
}
