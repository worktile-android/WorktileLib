package com.worktile.ui.recyclerview

import android.os.Handler
import androidx.recyclerview.widget.RecyclerView

const val TAG = "RecyclerView"

fun RecyclerView.executeAfterAllAnimationsAreFinished(
    callback: () -> Unit
) {
    Handler().post(object: Runnable {
        override fun run() {
            if (isAnimating) {
                itemAnimator?.isRunning {
                    Handler().post(this)
                }
                return
            }
            callback.invoke()
        }
    })
}