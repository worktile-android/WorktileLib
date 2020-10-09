package com.worktile.json.operator

import com.worktile.json.JsonDsl
import com.worktile.json.Parser
import com.worktile.json.ParserData
import java.lang.Exception
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty0

fun Any.parse(block: ParserOperator.() -> Unit) {
    val parserData = JsonDsl.jsonMap[this]
    parserData?.run {
        block.invoke(ParserOperator(parserData))
    } ?: throw Exception("没有找到json, $this")
}

class ParserOperator(parserData: ParserData) : Parser(parserData) {
    operator fun invoke(block: Parser.() -> Unit) {
        block.invoke(this)
    }

    inline infix operator fun <reified T> String.compareTo(property: KMutableProperty0<T>): Int {
        into(property)
        return 0
    }

    inline operator fun <reified T> String.compareTo(attachResult: AttachResult<T>): Int {
        val intoResult = into(attachResult.property)
        intoResult.attach(attachResult.attach)
        return 0
    }

    inline operator fun <reified T> KMutableProperty0<T>.invoke(
        noinline block: Parser.(t: T?) -> Unit
    ): AttachResult<T> {
        return AttachResult(this, block)
    }

    inner class AttachResult<T>(val property: KMutableProperty0<T>, val attach: Parser.(t: T?) -> Unit)

    infix operator fun String.invoke(block: Parser.() -> Unit): ThenResult {
        return then(block)
    }

    inline operator fun <reified T : Any> String.invoke(
        block: Parser.(t: T) -> Unit
    ): CustomParseResult<T> {
        return parse(block)
    }

    inline operator fun <reified T> CustomParseResult<T>.compareTo(property: KMutableProperty0<in T>): Int {
        into(property)
        return 0
    }

    infix operator fun String.div(key: String): AlterResult {
        return alter(key)
    }

    inline infix operator fun <reified T> ThenResult.compareTo(property: KMutableProperty0<T>): Int {
        into(property)
        return 0
    }

    inline infix operator fun <reified T> ThenResult.compareTo(attachResult: AttachResult<T>): Int {
        into(attachResult.property).attach(attachResult.attach)
        return 0
    }

    infix operator fun AlterResult.div(key: String): AlterResult {
        return alter(key)
    }

    inline infix operator fun <reified T> AlterResult.compareTo(property: KMutableProperty0<T>): Int {
        into(property)
        return 0
    }

    inline infix operator fun <reified T> AlterResult.compareTo(attachResult: AttachResult<T>): Int {
        into(attachResult.property).attach(attachResult.attach)
        return 0
    }

    inline infix operator fun <reified T> String.get(block: Parser.(t: T) -> Unit) {

    }
}
