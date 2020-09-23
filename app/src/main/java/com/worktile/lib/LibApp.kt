package com.worktile.lib

import android.app.Application
import com.worktile.base.Worktile

class LibApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Worktile.install(this)
    }
}