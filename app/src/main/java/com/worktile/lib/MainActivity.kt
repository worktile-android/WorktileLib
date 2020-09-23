package com.worktile.lib

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.worktile.base.Worktile
import com.worktile.base.arch.viewmodel.default
import com.worktile.ui.recyclerview.*
import com.worktile.ui.recyclerview.binder.bind
import com.worktile.ui.recyclerview.viewmodels.RecyclerViewViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val viewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(MainActivityViewModel::class.java)
        recycler_view.bind(viewModel, this) { type ->
            when (type) {
                TestItemViewModel::class -> {
                    TextView(this)
                }
                else -> null
            }
        }
    }
}

class MainActivityViewModel : ViewModel(), RecyclerViewViewModel by default() {
    private val originData = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")

    init {
        originData.forEach {
            recyclerViewData.value?.add(object : TestItemViewModel {
                override val title = MutableLiveData(it)
                override fun key() = it
            })
        }
        recyclerViewData.postValue(recyclerViewData.value)
    }
}

interface TestItemViewModel : DiffItemViewModel, Definition {
    val title: MutableLiveData<String>

    override fun bind(itemView: View) {
        (itemView as? TextView)?.text = title.value
    }

    override fun type() = TestItemViewModel::class
}
