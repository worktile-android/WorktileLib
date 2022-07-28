package com.worktile.common

import android.app.Application
import android.content.Context
import androidx.startup.Initializer

class WorktileInitializer : Initializer<WorktileInitializer> {
    override fun create(context: Context): WorktileInitializer {
        Worktile.install(context as Application)
        return this
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return mutableListOf()
    }

}