package com.worktile.ui.recyclerview

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SimpleAdapter<T>(
    private val data: MutableList<T>,
    private val itemViewCreator: (type: Any) -> View?
) : RecyclerView.Adapter<ItemViewHolder>() where T : ItemViewModel, T : ItemBinder {
    private val typeToAdapterTypeMap = hashMapOf<Any, Int>()
    private val adapterTypeToTypeMap = hashMapOf<Int, Any>()
    private var typeIndex = 0

    override fun getItemViewType(position: Int): Int {
        val itemData = data[position]
        typeToAdapterTypeMap[itemData.type()]?.run {
            return this
        } ?: run {
            val adapterType = typeIndex
            typeToAdapterTypeMap[itemData.type()] = adapterType
            adapterTypeToTypeMap[adapterType] = itemData.type()
            typeIndex++
            return adapterType
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val type = adapterTypeToTypeMap[viewType]!!
        val itemView = itemViewCreator.invoke(type)
        itemView?.run {
            return ItemViewHolder(itemView)
        } ?: run {
            throw RuntimeException("type为${type}时无法创建View")
        }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        data[position].bind(holder.itemView)
    }

    override fun getItemCount() = data.size

    fun updateData(newData: List<T>) = GlobalScope.launch {
        val diffResult = withContext(Dispatchers.Default) {
            DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize() = data.size

                override fun getNewListSize() = newData.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    return data[oldItemPosition].key() == newData[newItemPosition].key()
                }

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                    val oldContent = data[oldItemPosition].content()
                    val newContent = newData[newItemPosition].content()
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
                                Log.w("SimpleAdapter", "item ${data[oldItemPosition].key()}前后两次对比结果不同")
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

class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)