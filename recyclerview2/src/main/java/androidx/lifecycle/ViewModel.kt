package androidx.lifecycle

import java.io.Closeable

private const val CLEAR_CALLBACK_KEY = "com.worktile.ui.recyclerview.ViewModel.CLEAR_CALLBACK_KEY"

internal fun ViewModel.registerClearCallback(callback: () -> Unit) {
    setTagIfAbsent(CLEAR_CALLBACK_KEY, ClearCallbackWrapper(callback))
}

private class ClearCallbackWrapper(val callback: () -> Unit) : Closeable {
    override fun close() {
        callback.invoke()
    }
}