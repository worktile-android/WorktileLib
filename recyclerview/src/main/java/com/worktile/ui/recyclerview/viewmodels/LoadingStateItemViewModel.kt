package com.worktile.ui.recyclerview.viewmodels

import android.view.View
import com.worktile.ui.recyclerview.Definition
import com.worktile.ui.recyclerview.DiffItemViewModel

internal abstract class LoadingStateItemViewModel(private val key: Any) : DiffItemViewModel, Definition {
    override fun key(): Any = key
    override fun type() = key()
    override fun bind(itemView: View) {}
}