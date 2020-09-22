package com.worktile.ui.recyclerview

import android.view.View
import com.worktile.base.arch.viewmodel.BaseViewModel

/**
 * @param itemBinder 这里不推荐在可以复用的情况下使用lambda表达式，因为每个类型的itemBinder对应RecyclerView Adapter中的一个type
 */
data class ItemData<T : ItemViewModel>(
    val itemViewModel: T,
    val itemView: View,
    val itemBinder: ItemBinder<T>
)

fun interface ItemBinder<T : ItemViewModel> {
    /**
     * 该处不会出现类型错误的情况，参考[SimpleAdapter.onBindViewHolder]，itemBinder和itemViewHolder都被保存在同一个itemData中，
     * 因此取出的时候也不会出现类型不匹配的错误
     */
    fun bind(itemViewModel: @UnsafeVariance T, itemView: View)
}

interface ItemViewModel : BaseViewModel