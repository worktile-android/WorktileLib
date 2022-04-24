package com.worktile.common

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.worktile.common.arch.livedata.combineLiveData

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.worktile.base.test", appContext.packageName)
        val liveData1 = MutableLiveData<Int>()
        val liveData2 = MutableLiveData<String>()
        val me: MediatorLiveData<Any> = combineLiveData(liveData1, liveData2) { _, _, a ->
            a.postValue(Any())
        }
    }
}
