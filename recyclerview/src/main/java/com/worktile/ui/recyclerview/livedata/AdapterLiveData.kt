package com.worktile.ui.recyclerview.livedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.worktile.common.arch.livedata.LazyLiveData
import com.worktile.ui.recyclerview.ItemDefinition

class AdapterLiveData(
    value: AdapterLiveDataValue
) : LazyLiveData<AdapterLiveDataValue>(value) {
    class ObserverWrapper : Observer<AdapterLiveDataValue> {
        var realObserver = Observer<AdapterLiveDataValue> {  }

        override fun onChanged(t: AdapterLiveDataValue?) {
            realObserver.onChanged(t)
        }
    }

    internal val observerWrapper = ObserverWrapper()
    internal var pendingEdgeStateValue: AdapterLiveDataValue? = null

    internal fun internalPostValue(value: AdapterLiveDataValue) {
        postValue(value)
    }

    internal fun observe(owner: LifecycleOwner) {
        super.observe(owner, observerWrapper)
    }

    internal fun removeObserve() {
        innerRemoveObserver(observerWrapper)
    }

    internal fun postPendingEdgeStateValue() {
        pendingEdgeStateValue?.apply {
            internalPostValue(this)
        }
        pendingEdgeStateValue = null
    }
}

data class AdapterLiveDataValue(
    val items: List<ItemDefinition>,
    var updateCallback: () -> Unit = {}
)