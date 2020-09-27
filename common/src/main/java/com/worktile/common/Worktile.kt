package com.worktile.common

import android.app.Activity
import android.app.Application
import android.os.Bundle

object Worktile {
    val activityContext: Activity? get() = topActivity
    val applicationContext: Application? get() = application

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

            override fun onActivityDestroyed(activity: Activity) {
                super.onActivityDestroyed(activity)
                topActivity = null
            }
        }
        application.registerActivityLifecycleCallbacks(contextRecorder)
    }
}