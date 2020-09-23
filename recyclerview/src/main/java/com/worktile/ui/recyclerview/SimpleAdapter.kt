package com.worktile.ui.recyclerview

import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass

class SimpleAdapter<T : ItemViewModel>(
    private val data: MutableList<ItemData<out T>>
) : RecyclerView.Adapter<ItemViewHolder<T>>() {
    private val binderTypeMap = hashMapOf<KClass<*>, Int>()
    private val typeItemDataMap = hashMapOf<Int, ItemData<out T>>()
    private var typeIndex = 0

    override fun getItemViewType(position: Int): Int {
        val itemData = data[position]
        val itemBinder = itemData.itemBinder
        binderTypeMap[itemBinder::class]?.run {
            return this
        } ?: run {
            val type = typeIndex
            binderTypeMap[itemBinder::class] = type
            typeItemDataMap[type] = itemData
            typeIndex++
            return type
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder<T> {
        return ItemViewHolder(typeItemDataMap[viewType]!!)
    }

    override fun onBindViewHolder(holder: ItemViewHolder<T>, position: Int) {
        holder.itemData.itemBinder.bind(holder.itemData.itemViewModel, holder.itemData.itemView)
    }

    override fun getItemCount() = data.size

    fun updateData(newData: List<ItemData<out T>>) = GlobalScope.launch {
        val diffResult = withContext(Dispatchers.Default) {
            DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = data.size

                override fun getNewListSize() = newData.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return data[oldItemPosition].itemViewModel.key() == newData[newItemPosition].itemViewModel.key()
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val oldContent = data[oldItemPosition].itemViewModel.content()
                    val newContent = newData[newItemPosition].itemViewModel.content()
                    if (oldContent == null || newContent == null) {
                        return false
                    }
                    if (oldContent.size != newContent.size) {
                        return false
                    }

                    oldContent.forEachIndexed { index, oldContentItem ->
                        val newContentItem = newContent[index]
                        if (oldContentItem.value == null && newContentItem.value == null) {
                            return@forEachIndexed
                        }
                        if (oldContentItem.value == null || newContentItem.value == null) {
                            return false
                        }
                        if (oldContentItem.value::class != newContentItem.value::class) {
                            return false
                        }
                        val oldComparatorResult = oldContentItem.comparator?.invoke(
                            oldContentItem.value,
                            newContentItem.value
                        )
                        val newComparatorResult = newContentItem.comparator?.invoke(
                            oldContentItem.value,
                            newContentItem.value
                        )
                        if (oldComparatorResult != null && newComparatorResult != null) {
                            if (oldComparatorResult != newComparatorResult) {
                                Log.w("SimpleAdapter", "item ${data[oldItemPosition].itemViewModel.key()}前后两次对比结果不同")
                                return false
                            } else {
                                val result = oldComparatorResult && newComparatorResult
                                if (result) return@forEachIndexed else return false
                            }
                        } else if (oldComparatorResult == null && newComparatorResult == null) {
                            val result = oldContentItem.value == newContentItem.value
                            if (result) return@forEachIndexed else return false
                        } else {
                            val result = (oldComparatorResult ?: true) && (newComparatorResult ?: true)
                            if (result) return@forEachIndexed else return false
                        }
                    }

                    return true
                }
            })
        }
        data.clear()
        data.addAll(newData)
        diffResult.dispatchUpdatesTo(this@SimpleAdapter)
    }
}

class ItemViewHolder<T : ItemViewModel>(val itemData: ItemData<out T>) : RecyclerView.ViewHolder(itemData.itemView)