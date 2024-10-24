@file:Suppress("UNCHECKED_CAST")

package com.worktile.json

import com.worktile.json.JsonDsl.Companion.TAG
import com.worktile.json.JsonDsl.Companion.log
import org.json.JSONArray
import org.json.JSONObject
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubclassOf

internal fun KProperty<*>.actualClass(): KClass<*>? {
    return returnType.classifier as? KClass<*>
}

private class IntoBlockTemp {
    var value: Any? = null
}

private class DirectReturnTemp<T> {
    var value: T? = null
}

class Operation(val data: ParserData) {
    var errorFlags = 0b00000000

    fun <T> String.into(property: KMutableProperty0<T>, tkClass: KClass<*>): IntoResult<T> {
        val value = data.jsonObject.opt(this)
        value?.run {
            when (value) {
                is JSONObject -> {
                    val result = if (tkClass != Any::class) {
                        data.jsonDsl.parse(value, tkClass) as T
                    } else {
                        value as T
                    }
                    property.set(result)
                    return IntoResult(result)
                }
                is JSONArray -> {
                    when {
                        tkClass.isSubclassOf(List::class) -> {
                            val argumentType = property.returnType.arguments[0]
                            (argumentType.type?.classifier as? KClass<*>)?.apply {
                                val list = mutableListOf<Any>()
                                for (index in 0 until value.length()) {
                                    val itemJson = value[index] as JSONObject
                                    list.add(data.jsonDsl.parse(itemJson, this))
                                }
                                property.set(list as T)
                                return IntoResult(list)
                            } ?: run {
                                if (log) {
                                    println("${TAG}: 找不到泛型类型")
                                }
                            }
                        }
                        tkClass == Any::class -> {
                            property.set(value as T)
                        }
                        else -> {
                            if (log) {
                                println(
                                    "${TAG}: key ${this@into}对应的值是List类型，但${property}不是。jsonObject: ${data.jsonObject}"
                                )
                            } else {

                            }
                        }
                    }
                }
                is Number -> {
                    when {
                        tkClass.isSubclassOf(Number::class) -> {
                            val numberResult = when (tkClass) {
                                Int::class -> value.toInt()
                                Double::class -> value.toDouble()
                                Long::class -> value.toLong()
                                Float::class -> value.toFloat()
                                Short::class -> value.toShort()
                                Char::class -> value.toChar()
                                Byte::class -> value.toByte()
                                else -> 0
                            }
                            property.set(numberResult as T)
                            return IntoResult(numberResult)
                        }
                        tkClass == Any::class -> {
                            property.set(value as T)
                            return IntoResult(value)
                        }
                        else -> {
                            if (log) {
                                println("${TAG}: 需要一个Number类型属性，但${property}不是")
                            } else {

                            }
                        }
                    }
                }
                is String -> {
                    if (String::class == tkClass || tkClass == Any::class) {
                        property.set(value as T)
                        return IntoResult(value)
                    } else {
                        if (log) {
                            println("${TAG}: 需要一个String类型属性，但${property}不是")
                        } else {

                        }
                    }
                }
                is Boolean -> {
                    if (Boolean::class == tkClass || tkClass == Any::class) {
                        property.set(value as T)
                        return IntoResult(value)
                    } else {
                        if (log) {
                            println("${TAG}: 需要一个Boolean类型属性，但${property}不是")
                        } else {

                        }
                    }
                }
                JSONObject.NULL -> {
                }
                else -> {
                    if (log) {
                        println(
                            "${TAG}: 无法解析，key：${this@into}, value=${value::class} jsonObject: ${data.jsonObject}"
                        )
                    } else {

                    }
                }
            }
        } ?: run {
//            error(THROW_NOT_FOUNT_EXCEPTION, "key \"${this}\"不存在于${data.jsonObject}中")
        }
        return IntoResult()
    }

    fun String.intoBlock(block: (Any?) -> Unit) {
        IntoBlockTemp().apply {
            value = null
            into(::value, Any::class)
            block.invoke(value)
        }
    }

    fun <T> String.directReturn(tkClass: KClass<*>): T? {
        val keys = split('.')
        val directReturnTemp = DirectReturnTemp<T>()
        directReturnSplitBlock(0, keys, tkClass, directReturnTemp).invoke(Operation(data).apply {
            this.errorFlags = this@Operation.errorFlags
        })
        return directReturnTemp.value
    }

    private fun <T> directReturnSplitBlock(
        index: Int,
        keys: List<String>,
        tkClass: KClass<*>,
        directReturnTemp: DirectReturnTemp<T>
    ): (Operation) -> Unit {
        return if (index == keys.size - 1) {
            {
                it.apply {
                    keys[index].into(
                        directReturnTemp::value as KMutableProperty0<T>,
                        tkClass
                    )
                }
            }
        } else {
            {
                it.apply {
                    keys[index].then {
                        directReturnSplitBlock(index + 1, keys, tkClass, directReturnTemp)
                            .invoke(operation)
                    }
                }
            }
        }
    }

    inner class IntoResult<T>(val propertyValue: T? = null)

    fun <T> IntoResult<T>.attach(block: Parser.(t: T?) -> Unit) {
        block.invoke(Parser(data), this.propertyValue)
    }

    fun String.then(block: Parser.() -> Unit): ThenResult {
        val thenObject = data.jsonObject.opt(this@then)
        thenObject?.run {
            if (thenObject is JSONObject) {
                block(Parser(ParserData(data.jsonDsl, thenObject, this@then)))
            } else {
                if (log) {
                    println("${TAG}: key \"${this@then}\"对应的值不是JSONObject，无法执行then方法")
                }
            }
        } ?: run {
//            error(THROW_NOT_FOUNT_EXCEPTION, "key \"${this@then}\"不存在于${data.jsonObject}中")
        }
        return ThenResult(this)
    }

    inner class ThenResult(val key: String)

    fun <T> ThenResult.into(
        property: KMutableProperty0<T>,
        tkClass: KClass<*>
    ): IntoResult<T> {
        return key.into(property, tkClass)
    }

    fun String.alter(key: String): AlterResult {
        val alterResult = AlterResult()
        alterResult.alterKeys.run {
            add(this@alter)
            add(key)
        }
        return alterResult
    }

    inner class AlterResult {
        val alterKeys = mutableListOf<String>()
    }

    fun AlterResult.alter(key: String): AlterResult {
        alterKeys.add(key)
        return this
    }

    fun <T> AlterResult.into(
        property: KMutableProperty0<T>,
        tkClass: KClass<*>
    ): IntoResult<T> {
        alterKeys.forEach {
            if (data.jsonObject.has(it)) {
                return it.into(property, tkClass)
            }
        }
        return IntoResult()
    }

    fun AlterResult.intoBlock(block: (Any?) -> Unit) {
        IntoBlockTemp().apply {
            alterKeys.forEach {
                if (data.jsonObject.has(it)) {
                    return it.intoBlock(block)
                }
            }
        }
    }

    fun <T> String.parse(
        block: Parser.(t: T) -> Unit,
        tkClass: KClass<*>
    ): CustomParseResult<T> {
        val value = tkClass.java.newInstance() as T
        when (val thenObject = data.jsonObject.opt(this)) {
            null -> {
//                error(THROW_NOT_FOUNT_EXCEPTION, "key \"${this}\"不存在于${data.jsonObject}中")
            }
            is JSONObject -> {
                block.invoke(Parser(ParserData(data.jsonDsl, thenObject, this)), value)
            }
            else -> {
                if (log) {
                    println("${TAG}: key \"${this}\"对应的值不是JSONObject，无法执行then方法")
                }
            }
        }
        return CustomParseResult(value)
    }

    inner class CustomParseResult<T>(val value: T)

    fun <T> CustomParseResult<T>.into(property: KMutableProperty0<in T>): IntoResult<T> {
        property.set(value)
        return IntoResult(value)
    }

    fun String.foreach(block: Parser.() -> Unit) {
        when (val jsonArray = data.jsonObject.opt(this)) {
            null -> {
//                error(THROW_NOT_FOUNT_EXCEPTION, "key \"${this}\"不存在于${data.jsonObject}中")
            }
            is JSONArray -> {
                parseJsonArray(jsonArray, block)
            }
            else -> {
                if (log) {
                    println("${TAG}: key \"${this}\"对应的值不是JSONArray，无法执行foreach方法")
                }
            }
        }
    }

    fun AlterResult.foreach(block: Parser.() -> Unit) {
        var key: String? = null
        alterKeys.forEach { alterKey ->
            if (data.jsonObject.has(alterKey)) {
                key = alterKey
            }
        }
        key?.apply {
            foreach(block)
        } ?: run {
//            error(THROW_NOT_FOUNT_EXCEPTION, "提供的可选key ${alterKeys}都不存在")
        }
    }

    private fun parseJsonArray(
        jsonArray: JSONArray,
        block: Parser.() -> Unit
    ) {
        val length = jsonArray.length()
        for (index in 0 until length) {
            when (val item = jsonArray[index]) {
                is JSONObject -> {
                    val parserData = ParserData(
                        data.jsonDsl,
                        item,
                        indexInArray = index
                    )
                    block.invoke(Parser(parserData))
                }

                is JSONArray -> {
//                    parseJsonArray(item, block)
                }

//                else -> block.invoke(Parser(data), item)
            }
        }
    }

    private fun error(flag: Int, message: String) {
        // 2023-09-04 22:33:16 性能问题，暂时注释掉
//        if (ERROR_MASK and errorFlags == flag) {
//            when (flag) {
//                THROW_NOT_FOUNT_EXCEPTION -> throw NotFoundException(message)
//                else -> if (log) println("${TAG}: $message")
//            }
//        } else {
//            if (log) {
//                println("${TAG}: $message")
//            }
//        }
    }
}