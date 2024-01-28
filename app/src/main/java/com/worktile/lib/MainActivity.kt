package com.worktile.lib

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.worktile.common.default
import com.worktile.ui.recyclerview.*
import com.worktile.ui.recyclerview.data.EdgeState
import com.worktile.ui.recyclerview.data.LoadingState
import com.worktile.ui.recyclerview.group.GroupLiveData
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

class MainActivity : AppCompatActivity() {
    private val stateFlow = MutableStateFlow("0")

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val viewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(
            MainActivityViewModel::class.java
        )
        recycler_view.apply {
            bind(viewModel, this@MainActivity)
            setOnEdgeLoadMore {
                setEdgeState(EdgeState.LOADING)
                GlobalScope.launch {
                    withContext(Dispatchers.IO) { delay(1000) }
                    setEdgeState(EdgeState.FAILED)
                }
            }
//            setOnEdgeLoadMoreRetry {
//                GlobalScope.launch {
//                    withContext(Dispatchers.Default) { delay(1000) }
//                    println("current data size: ${data.size}")
//
//                    originData.forEach {
//                        data.add(object : Test2ItemViewModel {
//                            private val key = UUID.randomUUID()
//                            override val list = emptyList<String>()
//                            override var title = it
//                            override fun key() = key
//                        })
//                    }
//                    notifyChanged()
//                    setEdgeState(EdgeState.SUCCESS)
//                }
//            }
        }

        viewModel.originData.observe(this) { originData ->
            recycler_view.apply {
                val itemGroup = ItemGroup()
                itemGroup.setTitle(
                    object : Test2ItemViewModel {
                        override var title = "0000000"
                        override val list = emptyList<String>()
                        override fun key() = "1111111"
                    }
                )

                itemGroup.setItems(mutableListOf<ItemDefinition>().apply {
                    originData.forEach {
                        add(object : TestItemViewModel {
                            private val key = UUID.randomUUID()
                            override val title = MutableLiveData(it)
                            override fun key() = key
                        })
                    }
                })
                data.itemGroups.add(itemGroup)
                notifyChanged()
            }
        }

        button.setOnClickListener {
            recycler_view.apply {
                (data[0] as? Test2ItemViewModel)?.title = "2333=${System.currentTimeMillis()}"
                notifyChanged()
            }
        }
        loading.setOnClickListener {
//            recycler_view.setLoadingState(LoadingState.LOADING)
            startActivity(Intent(this, DemoActivity::class.java))
        }
        empty.setOnClickListener {
//            recycler_view.setLoadingState(LoadingState.EMPTY)
            (recycler_view.parent as ViewGroup).removeView(recycler_view)
        }
        failed.setOnClickListener {
            recycler_view.setLoadingState(LoadingState.FAILED)
        }
        footer_loading.setOnClickListener {
            recycler_view.setEdgeState(EdgeState.LOADING)
        }
        footer_no_more.setOnClickListener {
            recycler_view.setEdgeState(EdgeState.NO_MORE)
        }
        footer_failed.setOnClickListener {
            recycler_view.setEdgeState(EdgeState.FAILED)
        }
        footer_success.setOnClickListener {
            recycler_view.setEdgeState(EdgeState.SUCCESS)
        }
        viewModel.groupLiveData.observe(recycler_view,this) { group, t ->
            group.data.apply {

            }
        }
    }

    private suspend fun check(version: String) {
        println("check version: $version")
        withContext(Dispatchers.Default) {
            println("check version: $version, Coroutine 1")
            delay(500)
        }
        withContext(Dispatchers.Main) {
            println("check version: $version, Coroutine 2")
            delay(500)
        }
    }
}

class MainActivityViewModel : ViewModel(), RecyclerViewViewModel by default() {
    val originData = MutableLiveData(arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10" ,"16", "17", "18", "11", "101","9", "10" ,"16", "17", "18", "11", "101",
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" ,"16", "17", "18", "11", "101","9", "10" ,"16", "17", "18", "11", "101"))

    val groupLiveData = GroupLiveData<Any>()

    init {

//        loadingState set LoadingState.LOADING

    }
}

interface TestItemViewModel : ItemDefinition {
    val title: MutableLiveData<String>

    override fun viewCreator() = { parent: ViewGroup ->
        TextView(parent.context).apply {
            textSize = 30F
        }
    }

    override fun bind(itemView: View) {
        (itemView as? TextView)?.text = title.value
        itemAsync {
            main(itemView) {

            }
        }
    }

    override fun type(): Any = TestItemViewModel::class

    override fun content(): Array<ContentItem<*>>? {
        return arrayOf(
            ContentItem(title.value)
        )
    }
}

interface Test2ItemViewModel : ItemDefinition {
    var title: String
    val list: List<String>

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

    override fun content(): Array<ContentItem<*>>? {
        return arrayOf(
            ContentItem(title),
            ContentItem(list) { a, b ->
                if (a.size == b.size) {
                    a.forEachIndexed { index, s ->
                        if (index >= b.size || b[index] != s) {
                            return@ContentItem false
                        }
                    }
                    return@ContentItem true
                } else {
                    return@ContentItem false
                }
            }
        )
    }
}
