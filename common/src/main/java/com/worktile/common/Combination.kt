package com.worktile.common

data class Combination4<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
) {
    override fun toString() = "($first, $second, $third, $fourth)"
}

data class Combination5<out A, out B, out C, out D, out E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
) {
    override fun toString() = "($first, $second, $third, $fourth, $fifth)"
}

data class Combination6<out A, out B, out C, out D, out E, out F>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F
) {
    override fun toString() = "($first, $second, $third, $fourth, $fifth, $sixth)"
}

data class Combination7<out A, out B, out C, out D, out E, out F, out G>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F,
    val seventh: G
) {
    override fun toString() = "($first, $second, $third, $fourth, $fifth, $sixth, $seventh)"
}

data class Combination8<out A, out B, out C, out D, out E, out F, out G, out H>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F,
    val seventh: G,
    val eighth: H
) {
    override fun toString() = "($first, $second, $third, $fourth, $fifth, $sixth, $seventh, $eighth)"
}

data class Combination9<out A, out B, out C, out D, out E, out F, out G, out H, out I>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F,
    val seventh: G,
    val eighth: H,
    val ninth: I
) {
    override fun toString() = "($first, $second, $third, $fourth, $fifth, $sixth, $seventh, $eighth, $ninth)"
}