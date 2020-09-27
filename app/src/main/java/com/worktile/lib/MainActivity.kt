package com.worktile.lib

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.worktile.base.arch.livedata.notifyChanged
import com.worktile.base.arch.viewmodel.default
import com.worktile.ui.recyclerview.*
import com.worktile.ui.recyclerview.binder.bind
import com.worktile.ui.recyclerview.LoadingState
import com.worktile.ui.recyclerview.utils.set
import com.worktile.ui.recyclerview.viewmodels.RecyclerViewViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.util.*

class MainActivity : AppCompatActivity() {
    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val viewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(
            MainActivityViewModel::class.java
        )
        recycler_view.bind(viewModel, this)
        button.setOnClickListener {
            viewModel.updateData()
        }
        loading.setOnClickListener {
            viewModel.loading()
        }
        empty.setOnClickListener {
            viewModel.empty()
        }
        failed.setOnClickListener {
            viewModel.failed()
        }
        footer_loading.setOnClickListener {
            viewModel.footerLoading()
        }
        footer_no_more.setOnClickListener {
            viewModel.footerNoMore()
        }
        footer_failed.setOnClickListener {
            viewModel.footerFailed()
        }
        footer_success.setOnClickListener {
            viewModel.footerSuccess()
        }
    }
}

class MainActivityViewModel : ViewModel(), RecyclerViewViewModel by default() {
    private val originData = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10" ,"16", "17", "18", "11", "101","9", "10" ,"16", "17", "18", "11", "101",
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" ,"16", "17", "18", "11", "101","9", "10" ,"16", "17", "18", "11", "101")

    init {
        recyclerViewData.value?.add(object : Test2ItemViewModel {
            override var title = "0000000"
            override fun key() = "1111111"
        })

        originData.forEach {
            recyclerViewData.value?.add(object : TestItemViewModel {
                private val key = UUID.randomUUID()
                override val title = MutableLiveData(it)
                override fun key() = key
            })
        }
        recyclerViewData.notifyChanged()
    }


    fun updateData() {
        (recyclerViewData.value?.get(0) as? Test2ItemViewModel)?.title =
            "2333=${System.currentTimeMillis()}"
        recyclerViewData.notifyChanged()
//        recyclerViewData.value?.add(5, object : TestItemViewModel {
//            override val title = MutableLiveData("5678")
//            override fun key() = "5678"
//        })
//        recyclerViewData.notifyChanged()
    }

    fun loading() {
        loadingState.value = LoadingState.LOADING
    }

    fun empty() {
        loadingState.value = LoadingState.EMPTY
    }

    fun failed() {
        loadingState.value = LoadingState.FAILED
    }

    fun footerLoading() {
        footerState set EdgeState.LOADING
    }

    fun footerNoMore() {
        footerState set EdgeState.NO_MORE
    }

    fun footerFailed() {
        footerState set EdgeState.FAILED
    }

    fun footerSuccess() {
        footerState set EdgeState.SUCCESS
    }

    override val onLoadMore = {
        footerState set EdgeState.LOADING
        GlobalScope.launch {
            withContext(Dispatchers.IO) { delay(1000) }
            footerState set EdgeState.FAILED
        }
        Unit
    }

    override val onLoadMoreRetry = {
        GlobalScope.launch {
            withContext(Dispatchers.Default) { delay(1000) }
            println("current data size: ${recyclerViewData.value?.size}")
            footerState set EdgeState.SUCCESS
            originData.forEach {
                recyclerViewData.value?.add(object : Test2ItemViewModel {
                    private val key = UUID.randomUUID()
                    override var title = it
                    override fun key() = key
                })
            }
            recyclerViewData.notifyChanged()
        }
        Unit
    }
}

interface TestItemViewModel : DiffLiveDataItemViewModel, Definition {
    val title: MutableLiveData<String>

    override fun viewCreator() = { parent: ViewGroup ->
        TextView(parent.context).apply {
            textSize = 30F
        }
    }

    override fun bind(itemView: View) {
        (itemView as? TextView)?.text = title.value
    }

    override fun type(): Any = TestItemViewModel::class
}

interface Test2ItemViewModel : DiffItemViewModel, Definition {
    var title: String

    override fun viewCreator() = { parent: ViewGroup ->
        TextView(parent.context).apply {
            textSize = 30F
            setTextColor(Color.BLUE)
        }
    }

    override fun bind(itemView: View) {
        (itemView as? TextView)?.text = title
    }

    override fun type(): Any = Test2ItemViewModel::class
}
