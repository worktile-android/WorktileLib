package com.worktile.ui.recyclerview

import android.os.Handler
import androidx.recyclerview.widget.RecyclerView

const val TAG = "RecyclerView"

fun RecyclerView.executeAfterAllAnimationsAreFinished(
    callback: () -> Unit
) {
    lateinit var waitForAnimationsToFinishRunnable: Runnable
    waitForAnimationsToFinishRunnable = Runnable {
        if (isAnimating) {
            itemAnimator?.isRunning {
                Handler().post(waitForAnimationsToFinishRunnable)
            }
            return@Runnable
        }
        callback.invoke()
    }
    Handler().post(waitForAnimationsToFinishRunnable)
}