package com.worktile.common.arch.viewmodel

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

inline fun <reified T : ViewModel> viewModel(
    owner: ViewModelStoreOwner,
    key: String? = null,
    noinline factory: (() -> T)? = null
): T {
    val provider = factory?.run {
        ViewModelProvider(owner, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                return factory.invoke() as T
            }
        })
    } ?: run {
        ViewModelProvider(owner)
    }

    return key?.run { provider.get(this, T::class.java) } ?: provider.get(T::class.java)
}

inline fun <reified T : ViewModel> Fragment.viewModel(
    key: String? = null,
    noinline factory: (() -> T)? = null
): T {
    return viewModel(this, key, factory)
}

inline fun <reified T : ViewModel> AppCompatActivity.viewModel(
    key: String? = null,
    noinline factory: (() -> T)? = null
): T {
    return viewModel(this, key, factory)
}

inline fun <reified T : ViewModel> viewModel(
    store: ViewModelStore,
    key: String? = null,
    noinline factory: (() -> T),
): T {
    val provider = ViewModelProvider(store, object : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return factory.invoke() as T
        }
    })

    return key?.run { provider.get(this, T::class.java) } ?: provider.get(T::class.java)
}