package com.worktile.common.arch.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import kotlin.experimental.ExperimentalTypeInference

fun <I1, I2, O> combineLiveData(
    input1: LiveData<I1>,
    input2: LiveData<I2>,
    combine:  MediatorLiveData<O>.(I1?, I2?) -> Unit
) = MediatorLiveData<O>().apply {
    addSource(input1) {
        combine(input1.value, input2.value)
    }
    addSource(input2) {
        combine(input1.value, input2.value)
    }
}

fun <I1, I2, I3, O> combineLiveData(
    input1: LiveData<I1>,
    input2: LiveData<I2>,
    input3: LiveData<I3>,
    combine: MediatorLiveData<O>.(I1?, I2?, I3?) -> Unit
) = MediatorLiveData<O>().apply {
    addSource(input1) {
        combine(input1.value, input2.value, input3.value)
    }
    addSource(input2) {
        combine(input1.value, input2.value, input3.value)
    }
    addSource(input3) {
        combine(input1.value, input2.value, input3.value)
    }
}

fun <I1, I2, I3, I4, O> combineLiveData(
    input1: LiveData<I1>,
    input2: LiveData<I2>,
    input3: LiveData<I3>,
    input4: LiveData<I4>,
    combine: MediatorLiveData<O>.(I1?, I2?, I3?, I4?) -> Unit
) = MediatorLiveData<O>().apply {
    addSource(input1) {
        combine(input1.value, input2.value, input3.value, input4.value)
    }
    addSource(input2) {
        combine(input1.value, input2.value, input3.value, input4.value)
    }
    addSource(input3) {
        combine(input1.value, input2.value, input3.value, input4.value)
    }
    addSource(input4) {
        combine(input1.value, input2.value, input3.value, input4.value)
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
    ) { input1, input2->
        onCombine(input1, input2)
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