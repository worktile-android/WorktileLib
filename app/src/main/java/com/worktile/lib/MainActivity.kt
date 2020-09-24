package com.worktile.lib

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.worktile.base.arch.notifyChanged
import com.worktile.base.arch.viewmodel.default
import com.worktile.ui.recyclerview.Definition
import com.worktile.ui.recyclerview.DiffItemViewModel
import com.worktile.ui.recyclerview.binder.bind
import com.worktile.ui.recyclerview.viewmodels.RecyclerViewViewModel
import kotlinx.android.synthetic.main.activity_main.*

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
    }
}

class MainActivityViewModel : ViewModel(), RecyclerViewViewModel by default() {
    private val originData = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10" ,"16", "17", "18", "11", "101","9", "10" ,"16", "17", "18", "11", "101")

    init {
        originData.forEach {
            recyclerViewData.value?.add(object : TestItemViewModel {
                override val title = MutableLiveData(it)
                override fun key() = it
            })
        }
        recyclerViewData.notifyChanged()
    }


    fun updateData() {
        (recyclerViewData.value?.get(2) as TestItemViewModel).title.value =
            "2333=${System.currentTimeMillis()}"
//        recyclerViewData.value?.add(5, object : TestItemViewModel {
//            override val title = MutableLiveData("5678")
//            override fun key() = "5678"
//        })
//        recyclerViewData.notifyChanged()
    }
}

interface TestItemViewModel : DiffItemViewModel, Definition {
    val title: MutableLiveData<String>

    override fun viewCreator() = { parent: ViewGroup ->
        TextView(parent.context).apply {
            textSize = 30F
        }
    }

    override fun bind(itemView: View) {
        title.observeForever {
            (itemView as? TextView)?.text = it
        }
    }

    override fun type() = TestItemViewModel::class
}
