package com.worktile.json

import com.worktile.json.*
import com.worktile.json.Operation.*
import java.lang.Exception
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty0

fun Any.parse(block: Parser.() -> Unit) {
    val parserData = JsonDsl.jsonMap[this]
    parserData?.run {
        block.invoke(Parser(parserData))
    } ?: throw Exception("没有找到json, $this")
}

class Parser(data: ParserData) {
    val operation = Operation(data)

    operator fun invoke(block: Parser.() -> Unit) {
        block.invoke(this)
    }

    infix operator fun <T> String.compareTo(property: KMutableProperty0<T>): Int {
        operation.apply { into(property) }
        return 0
    }

    operator fun String.compareTo(block: (Any?) -> Unit): Int {
        operation.apply { intoBlock(block) }
        return 0
    }

    operator fun <T> String.compareTo(attachResult: AttachResult<T>): Int {
        operation.apply {
            val intoResult = into(attachResult.property)
            intoResult.attach(attachResult.attach)
        }
        return 0
    }

    operator fun <T> KMutableProperty0<T>.invoke(
        block: Parser.(t: T?) -> Unit
    ): AttachResult<T> {
        return AttachResult(this, block)
    }

    inner class AttachResult<T>(val property: KMutableProperty0<T>, val attach: Parser.(t: T?) -> Unit)

    infix operator fun String.invoke(block: Parser.() -> Unit): ThenResult {
        return operation.run { then(block) }
    }

    inline operator fun <reified T : Any> String.invoke(
        block: Parser.(t: T) -> Unit
    ): CustomParseResult<T> {
        return operation.run { parse(block) }
    }

    operator fun <T> CustomParseResult<T>.compareTo(property: KMutableProperty0<in T>): Int {
        operation.apply { into(property) }
        return 0
    }

    infix operator fun String.div(key: String): AlterResult {
        return operation.run { alter(key) }
    }

    infix operator fun <T> ThenResult.compareTo(property: KMutableProperty0<T>): Int {
        operation.apply { into(property) }
        return 0
    }

    infix operator fun <T> ThenResult.compareTo(attachResult: AttachResult<T>): Int {
        operation.apply {
            into(attachResult.property).attach(attachResult.attach)
        }
        return 0
    }

    infix operator fun AlterResult.div(key: String): AlterResult {
        return operation.run { alter(key) }
    }

    infix operator fun <T> AlterResult.compareTo(property: KMutableProperty0<T>): Int {
        operation.run { into(property) }
        return 0
    }

    operator fun AlterResult.compareTo(block: (Any?) -> Unit): Int {
        operation.run { intoBlock(block) }
        return 0
    }

    infix operator fun <T> AlterResult.compareTo(attachResult: AttachResult<T>): Int {
        operation.run { into(attachResult.property).attach(attachResult.attach) }
        return 0
    }

    infix operator fun String.get(block: Parser.() -> Unit) {
        operation.run { foreach(block) }
    }

    inline infix operator fun <reified T> String.get(block: Parser.(t: T) -> Unit) {

    }
}
