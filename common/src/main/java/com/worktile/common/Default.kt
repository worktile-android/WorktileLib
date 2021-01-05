@file:Suppress("IMPLICIT_CAST_TO_ANY")

package com.worktile.common

import java.lang.Exception
import kotlin.reflect.full.*

annotation class Default

inline fun <reified T> default(): T {
    T::class.companionObject?.functions?.forEach { function ->
        function.annotations.forEach { annotation ->
            if (annotation.annotationClass == Default::class
                    && function.returnType.isSubtypeOf(T::class.createType())) {
                return function.call(T::class.companionObjectInstance) as T
            }
        }
    }
    throw Exception("cannot find default by ${T::class.qualifiedName}")
}