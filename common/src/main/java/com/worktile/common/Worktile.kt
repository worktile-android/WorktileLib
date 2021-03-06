package com.worktile.common

import android.app.Activity
import android.app.Application
import android.os.Bundle

object Worktile {
    val activityContext: Activity get() = topActivity ?: throw Exception()
    val applicationContext: Application get() = application ?: throw Exception()

    private var topActivity: Activity? = null
    private var application: Application? = null

    fun install(application: Application) {
        this.application = application
        val contextRecorder = object : ActivityLifecycleCallbacks() {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                super.onActivityCreated(activity, savedInstanceState)
                topActivity = activity
            }

            override fun onActivityStarted(activity: Activity) {
                super.onActivityStarted(activity)
                topActivity = activity
            }

            override fun onActivityResumed(activity: Activity) {
                super.onActivityResumed(activity)
                topActivity = activity
            }
        }
        application.registerActivityLifecycleCallbacks(contextRecorder)
    }
}