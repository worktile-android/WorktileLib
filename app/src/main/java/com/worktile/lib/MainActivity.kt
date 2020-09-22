package com.worktile.lib

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.worktile.base.arch.viewmodel.default
import com.worktile.ui.recyclerview.binder.bind
import com.worktile.ui.recyclerview.viewmodels.RecyclerViewViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val viewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(MainActivityViewModel::class.java)
        recycler_view.bind(viewModel.recyclerViewData, this)
    }
}

class MainActivityViewModel : ViewModel(), RecyclerViewViewModel by default() {
    init {
    }
}
