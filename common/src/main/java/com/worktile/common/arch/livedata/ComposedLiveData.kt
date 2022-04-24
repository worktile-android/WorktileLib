package com.worktile.common.arch.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

fun <I1, I2, O> composeLiveData(
    input1: LiveData<I1>,
    input2: LiveData<I2>,
    compose: (I1?, I2?) -> Unit
) = MediatorLiveData<O>().apply {
    addSource(input1) {
        compose(input1.value, input2.value)
    }
    addSource(input2) {
        compose(input1.value, input2.value)
    }
}

fun <I1, I2, I3, O> composeLiveData(
    input1: LiveData<I1>,
    input2: LiveData<I2>,
    input3: LiveData<I3>,
    compose: (I1?, I2?, I3?) -> Unit
) = MediatorLiveData<O>().apply {
    addSource(input1) {
        compose(input1.value, input2.value, input3.value)
    }
    addSource(input2) {
        compose(input1.value, input2.value, input3.value)
    }
    addSource(input3) {
        compose(input1.value, input2.value, input3.value)
    }
}

fun <I1, I2, I3, I4, O> composeLiveData(
    input1: LiveData<I1>,
    input2: LiveData<I2>,
    input3: LiveData<I3>,
    input4: LiveData<I4>,
    compose: (I1?, I2?, I3?, I4?) -> Unit
) = MediatorLiveData<O>().apply {
    addSource(input1) {
        compose(input1.value, input2.value, input3.value, input4.value)
    }
    addSource(input2) {
        compose(input1.value, input2.value, input3.value, input4.value)
    }
    addSource(input3) {
        compose(input1.value, input2.value, input3.value, input4.value)
    }
    addSource(input4) {
        compose(input1.value, input2.value, input3.value, input4.value)
    }
}