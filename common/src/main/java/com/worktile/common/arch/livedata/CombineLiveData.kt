package com.worktile.common.arch.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import kotlin.experimental.ExperimentalTypeInference

fun <I1, I2, O> combineLiveData(
    input1: LiveData<I1>,
    input2: LiveData<I2>,
    combine: (I1?, I2?, MediatorLiveData<O>) -> Unit
) = MediatorLiveData<O>().apply {
    addSource(input1) {
        combine(input1.value, input2.value, this)
    }
    addSource(input2) {
        combine(input1.value, input2.value, this)
    }
}

fun <I1, I2, I3, O> combineLiveData(
    input1: LiveData<I1>,
    input2: LiveData<I2>,
    input3: LiveData<I3>,
    combine: (I1?, I2?, I3?, MediatorLiveData<O>) -> Unit
) = MediatorLiveData<O>().apply {
    addSource(input1) {
        combine(input1.value, input2.value, input3.value, this)
    }
    addSource(input2) {
        combine(input1.value, input2.value, input3.value, this)
    }
    addSource(input3) {
        combine(input1.value, input2.value, input3.value, this)
    }
}

fun <I1, I2, I3, I4, O> combineLiveData(
    input1: LiveData<I1>,
    input2: LiveData<I2>,
    input3: LiveData<I3>,
    input4: LiveData<I4>,
    combine: (I1?, I2?, I3?, I4?, MediatorLiveData<O>) -> Unit
) = MediatorLiveData<O>().apply {
    addSource(input1) {
        combine(input1.value, input2.value, input3.value, input4.value, this)
    }
    addSource(input2) {
        combine(input1.value, input2.value, input3.value, input4.value, this)
    }
    addSource(input3) {
        combine(input1.value, input2.value, input3.value, input4.value, this)
    }
    addSource(input4) {
        combine(input1.value, input2.value, input3.value, input4.value, this)
    }
}

@OptIn(ExperimentalTypeInference::class)
fun <T, R, O> LiveData<T>.combine(
    liveData: LiveData<R>,
    @BuilderInference onCombine: MediatorLiveData<O>.(T?, R?) -> Unit
): MediatorLiveData<O> {
    return combineLiveData(
        this,
        liveData
    ) { input1, input2, output: MediatorLiveData<O> ->
        output.onCombine(input1, input2)
    }
}

fun <T, R> LiveData<T>.changedWith(liveData: LiveData<R>): LiveData<CombinationData<T, R>> {
    return combine(liveData) { t, r ->
        postValue(CombinationData(t, r))
    }
}

class CombinationData<T, R>(
    val data: T,
    val addition: R
) {
    operator fun component1(): T = data
    operator fun component2(): R = addition
}