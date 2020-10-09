package com.worktile.json

import org.junit.Test

import org.junit.Assert.*
import org.junit.runners.Parameterized
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty0
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmName

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    data class User(val name: String)

    val users: List<User> = emptyList()

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun test() {
        typeTest(::users)
    }

    private inline fun <reified T> typeTest(property0: KProperty0<T>) {
        println((property0.returnType.arguments[0].type?.classifier as? KClass<*>)?.java)
    }
}