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

/**
 * example:
 * ```
 * class User {
 *     var name: String = ""
 * }
 *
 * fun <T> parse(jsonObject: JSONObject, block: Parser.() -> T): T {
 *     Parser(ParserData(JsonDsl(), jsonObject)).apply {
 *         block.invoke(this)
 *     }
 * }
 *
 * userJson:
 * {
 *   "user": {
 *     "name": "hhhh",
 *     "children": [
 *       {
 *         "name": "aaaa",
 *         "age": 1
 *       },
 *       {
 *         "name": "bbbb",
 *         "age": 2
 *       }
 *     ]
 *   }
 * }
 * 或者有时：
 * {
 *   "user": {
 *     "_name": "hhhh"
 *   }
 * }
 * ```
 *
 * 使用操作符dsl解析必须要在含有Parser的scope中，以下示例代码默认在这样的scope中执行
 */
class Parser(data: ParserData) {
    val operation = Operation(data)

    operator fun invoke(block: Parser.() -> Unit) {
        block.invoke(this)
    }

    /**
     * example:
     * ```
     * "name" > user::name
     * ```
     * 根据key解析出的结果直接放入对象的属性中
     * @param property 对象的可变属性
     */
    inline operator fun <reified T> String.compareTo(property: KMutableProperty0<T>): Int {
        operation.apply { into(property, T::class) }
        return 0
    }

    /**
     * example:
     * ```
     * "name" > { name ->
     *     println(name as? String)
     * }
     * ```
     * 根据key解析出的结果作为参数传入后置的[block]中，并执行[block]，[block]中没有Parser对象，如果需要继续
     * dsl编写解析代码，需要手动创建Parser
     * @param block 接收解析结果的代码块
     */
    operator fun String.compareTo(block: (Any?) -> Unit): Int {
        operation.apply { intoBlock(block) }
        return 0
    }

    /**
     * example:
     * ```
     * "name" > user::name {
     *     println(it.name)
     * }
     * ```
     * 根据key解析出的结果放入对象的属性后，执行代码块，结果可以在代码块中通过it获取，代码块含有Parser对象，
     * parser对象的环境是当前key对应的环境
     *
     * String.compareTo对应的是">"操作符
     */
    inline operator fun <reified T> String.compareTo(attachResult: AttachResult<T>): Int {
        operation.apply {
            val intoResult = into(attachResult.property, T::class)
            intoResult.attach(attachResult.attach)
        }
        return 0
    }

    /**
     * 参见 String.compareTo(AttachResult)
     *
     * KMutableProperty0<T>.invoke对应的是"{}"操作符
     */
    operator fun <T> KMutableProperty0<T>.invoke(
        block: Parser.(t: T?) -> Unit
    ): AttachResult<T> {
        return AttachResult(this, block)
    }

    inner class AttachResult<T>(val property: KMutableProperty0<T>, val attach: Parser.(t: T?) -> Unit)

    /**
     * example:
     * ```
     * val name = "name"() ?: ""
     * ```
     * key解析出的结果作为函数的返回值返回，返回值可能为空
     *
     * 该方法支持连续key:
     * ```
     * val name = "user.name"() ?: ""
     * ```
     */
    inline operator fun <reified T> String.invoke(): T? {
        return operation.run { directReturn(T::class) }
    }

    /**
     * example:
     * ```
     * val name = "name"("")
     * ```
     * key解析出的结果作为函数的返回值返回，解析值为空时返回[defaultValue]。该方法同样支持连续key
     */
    inline operator fun <reified T> String.invoke(defaultValue: T): T {
        return operation.run { directReturn(T::class) ?: defaultValue }
    }

    /**
     * example:
     * ```
     * "user" {
     *     "name" > user::name
     * }
     * ```
     * key解析出结果之后，结果作为下一层的ParserData继续进行[block]中的解析。
     *
     * "String.invoke"对应的是"\"user\" {}"中的"{}"操作符
     */
    operator fun String.invoke(block: Parser.() -> Unit): ThenResult {
        return operation.run { then(block) }
    }

    /**
     * example:
     * ```
     * "user" <User> {
     *     "name" > it::name
     * }
     * ```
     * 指定一个类型，会自动反射构建一个该类型的对象，然后执行[block]，[block]接收刚刚创建的对象，用it可以接收到，另外，
     * 还含有parser对象，环境是key对应的环境。
     */
    inline operator fun <reified T> String.invoke(
        noinline block: Parser.(t: T) -> Unit
    ): CustomParseResult<T> {
        return operation.run { parse(block, T::class) }
    }

    /**
     * example:
     * ```
     * "user" <user> {
     *     "name" > it::name
     * } > ::user2
     * ```
     * 将经过String.invoke(Parser.(T) -> Unit)解析出的对象，也就是反射构建的对象放入[property]中
     *
     * "CustomParseResult<T>.compareTo"对应的操作符是"> ::user"中的">"
     */
    operator fun <T> CustomParseResult<T>.compareTo(property: KMutableProperty0<in T>): Int {
        operation.apply { into(property) }
        return 0
    }

    /**
     * example:
     * ```
     * "name" / "_name" > user::name
     * ```
     * 设置key的别名
     *
     * String.div对应的操作符是"/"
     */
    operator fun String.div(key: String): AlterResult {
        return operation.run { alter(key) }
    }

    /**
     * example:
     * ```
     * "user" {
     *     "name" > user::name
     * } > ::user2
     * ```
     * 在使用then操作符连续解析之后，还可以把当前key对应的结果解析到[property]中
     *
     * ThenResult.compareTo对应的操作符是"} > ::user2"中的">"
     */
    inline operator fun <reified T> ThenResult.compareTo(property: KMutableProperty0<T>): Int {
        operation.apply { into(property, T::class) }
        return 0
    }

    /**
     * example:
     * ```
     * "user" {
     *     "name" > user::name
     * } > ::user2 {
     *     // do something
     * }
     * ```
     * 在使用then操作符连续解析之后，还可以把当前key对应的结果解析到user2中，并且还可以进行附加操作，和
     * String.compareTo(AttachResult)类似
     *
     * ThenResult.compareTo(AttachResult)对应的操作符是"} > ::user2 {"中的">"
     */
    inline operator fun <reified T> ThenResult.compareTo(attachResult: AttachResult<T>): Int {
        operation.apply {
            into(attachResult.property, T::class).attach(attachResult.attach)
        }
        return 0
    }

    /**
     * example:
     * ```
     * "name" / "_name" / "_name_" / "_name2" > user::name
     * ```
     * 连续设置key的别名
     */
    operator fun AlterResult.div(key: String): AlterResult {
        return operation.run { alter(key) }
    }

    /**
     * example:
     * ```
     * "name" / "_name" / "_name_" / "_name2" > user::name
     * ```
     * 连续设置key的别名后解析到[property]中
     *
     * AlterResult.compareTo(KMutableProperty<0>)对应的操作符是">"
     */
    inline operator fun <reified T> AlterResult.compareTo(property: KMutableProperty0<T>): Int {
        operation.run { into(property, T::class) }
        return 0
    }

    /**
     * example:
     * ```
     * "name" / "_name" / "_name_" / "_name2" > { name ->
     *     println(name as? String)
     * }
     * ```
     * 连续设置key的别名后把解析到结果放入[block]中，和String.compareTo((Any?) -> Unit)类似
     */
    operator fun AlterResult.compareTo(block: (Any?) -> Unit): Int {
        operation.run { intoBlock(block) }
        return 0
    }

    /**
     * example:
     * ```
     * "name" / "_name" / "_name_" / "_name2" > user::name {
     *     // do something
     * }
     * ```
     * 连续设置key的别名后解析到user::name中，然后进行附加操作，和String.compareTo(AttachResult)类似
     *
     * AlterResult.compareTo(KMutableProperty<0>)对应的操作符是">"
     */
    inline operator fun <reified T> AlterResult.compareTo(attachResult: AttachResult<T>): Int {
        operation.run { into(attachResult.property, T::class).attach(attachResult.attach) }
        return 0
    }

    /**
     * example:
     * ```
     * "children"[{
     *     val child = Child()
     *     "age" > child::age
     * }]
     * ```
     * 对JSONArray进行解析，[block]中是对array中的每一个元素进行的操作，包含parser对象，环境是array中的每一个元素
     */
    operator fun String.get(block: Parser.() -> Unit) {
        operation.run { foreach(block) }
    }

    inline operator fun <reified T> String.get(block: Parser.(t: T) -> Unit) {

    }
}
