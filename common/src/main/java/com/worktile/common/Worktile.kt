package com.worktile.common

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log

@SuppressLint("StaticFieldLeak")
object Worktile {
    val activityContext: Activity get() = topActivity ?: throw Exception()
    val applicationContext: Application get() = application ?: throw Exception()

    private var topActivity: Activity? = null
    private var application: Application? = null

    fun install(application: Application) {
        if (this.application != null) {
            Log.e("Worktile", "不要重复初始化Worktile")
            return
        }
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