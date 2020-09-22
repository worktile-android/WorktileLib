package com.worktile.lib

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.worktile.base.Worktile
import com.worktile.base.arch.viewmodel.default
import com.worktile.ui.recyclerview.ItemBinder
import com.worktile.ui.recyclerview.ItemData
import com.worktile.ui.recyclerview.ItemViewModel
import com.worktile.ui.recyclerview.binder.bind
import com.worktile.ui.recyclerview.viewmodels.RecyclerViewViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val viewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(MainActivityViewModel::class.java)
        recycler_view.bind(viewModel, this)
    }
}

class MainActivityViewModel : ViewModel(), RecyclerViewViewModel by default() {
    val context = Worktile.applicationContext

    init {
        val item1 = ItemData(object : ItemViewModel {}, TextView(context)) { itemViewModel, itemView ->

        }
        val item2 = ItemData(TestItemView(), View(context), { t, itemView ->  })
        recyclerViewData.value?.add(item1)
        recyclerViewData.value?.add(item2)
    }
}

class TestItemView : ItemViewModel {

}
