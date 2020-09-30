package com.worktile.json.dsl

import android.util.Log
import com.worktile.json.JsonDsl
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import kotlin.reflect.KMutableProperty0

const val TAG = "JsonDsl"

fun Any.parse(block: Parser.() -> Unit) {
    val jsonObject = JsonDsl.jsonMap[this]
    jsonObject?.run {
        block.invoke(Parser(this))
    } ?: throw Exception("没有找到json, $this")
}

class Parser(val objectFromJson: Any) {
    inline infix fun <reified T> String.into(property: KMutableProperty0<T>) {
        (objectFromJson as? JSONObject)?.also {
            val value = it.opt(this)
            when (T::class) {
                Number::class -> {}
                String::class -> {}
                else -> {}
            }
        } ?: Log.w(TAG, "执行String.into方法寻找key为${this}的值时，需要一个JSNObject类型对象，但${objectFromJson}不是")
    }

    infix fun String.then(block: Parser.() -> Unit): ThenResult {
        (objectFromJson as? JSONObject)?.run {
            val thenObject = opt(this@then)
            thenObject?.run {
                block(Parser(thenObject))
            } ?: Log.w(TAG, "key \"${this@then}\"不存在于${objectFromJson}中")
        } ?: Log.w(TAG, "执行then方法寻找key为${this}的值时，需要一个JSNObject类型对象，但${objectFromJson}不是")
        return ThenResult(this)
    }

    inner class ThenResult(val key: String)
    inline infix fun <reified T> ThenResult.into(property: KMutableProperty0<T>) {
        key.into(property)
    }

    infix fun String.alter(key: String): AlterResult {
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

    infix fun AlterResult.alter(key: String): AlterResult {
        alterKeys.add(key)
        return this
    }

    inline infix fun <reified T> AlterResult.into(property: KMutableProperty0<T>) {
        (objectFromJson as? JSONObject)?.run {
            alterKeys.forEach {
                if (objectFromJson.has(it)) {
                    it.into(property)
                    return@run
                }
            }
        } ?: Log.w(TAG, "执行AlterResult.into方法需要一个JSNObject类型对象，但${objectFromJson}不是")
    }

    infix fun String.foreach(a: Any) {

    }
}