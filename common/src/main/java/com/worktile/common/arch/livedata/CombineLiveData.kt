package com.worktile.common.arch.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

fun <I1, I2, O : Any> combineLiveData(
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