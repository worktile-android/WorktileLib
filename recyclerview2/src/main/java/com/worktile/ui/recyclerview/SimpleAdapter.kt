package com.worktile.ui.recyclerview

import android.util.Log
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.*
import androidx.recyclerview.widget.*
import kotlinx.coroutines.*

class SimpleAdapter<T>(
    var data: List<T>,
    private val log: Boolean = false
) : RecyclerView.Adapter<SimpleAdapter.ItemViewHolder<T>>(), LifecycleObserver where T : ItemViewModel, T : ItemBinder {
    private val typeToAdapterTypeMap = hashMapOf<Any, Int>()
    private val adapterTypeToItemDataMap = hashMapOf<Int, T>()
    private var typeIndex = 0
    private var contentSparseArray = SparseArray<Array<ContentItem<*>>?>()
    private var recyclerView: RecyclerView? = null
    private val oldData = mutableListOf<T>()

    override fun getItemViewType(position: Int): Int {
        val itemData = data[position]
        typeToAdapterTypeMap[itemData.type()]?.run {
            return this
        } ?: run {
            val adapterType = typeIndex
            typeToAdapterTypeMap[itemData.type()] = adapterType
            adapterTypeToItemDataMap[adapterType] = itemData
            typeIndex++
            return adapterType
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder<T> {
        val itemData = adapterTypeToItemDataMap[viewType]!!
        return ItemViewHolder(itemData.viewCreator().invoke(parent))
    }

    override fun onBindViewHolder(holder: ItemViewHolder<T>, position: Int) {
        data[position].apply {
            contentSparseArray.put(position, content())
            bind(holder.itemView)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onViewAttachedToWindow(holder: ItemViewHolder<T>) {
        super.onViewAttachedToWindow(holder)
        holder.itemData?.attach(holder.itemView)
    }

    override fun onViewDetachedFromWindow(holder: ItemViewHolder<T>) {
        super.onViewDetachedFromWindow(holder)
        holder.itemData?.detach(holder.itemView)
    }

    override fun onViewRecycled(holder: ItemViewHolder<T>) {
        super.onViewRecycled(holder)
        holder.itemData?.recycle(holder.itemView)
    }

    suspend fun updateData(newData: List<T>, debugKey: String? = null, updateCallback: (() -> Unit)? = null) {
        val diffResult = withContext(Dispatchers.Default) {
            oldData.apply {
                clear()
                addAll(data)
            }
            if (BuildConfig.DEBUG && log) {
                Log.d(TAG, "calculateDiff, debugKey = $debugKey" +
                        ", thread = { id: ${Thread.currentThread().id}, name: ${Thread.currentThread().name}}")
            }
            DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int {
                    val size = oldData.size
                    if (BuildConfig.DEBUG && log) {
                        Log.d(TAG, "oldDataSize: $size")
                    }
                    return size
                }

                override fun getNewListSize(): Int {
                    val size = newData.size
                    if (BuildConfig.DEBUG && log) {
                        Log.d(TAG, "newDataSize: $size")
                    }
                    return size
                }

                override fun areItemsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    val oldKey = oldData[oldItemPosition].key()
                    val newKey = newData[newItemPosition].key()
                    if (BuildConfig.DEBUG && log) {
                        Log.d(TAG, "oldKey: $oldKey, newKey: $newKey")
                    }
                    return oldKey == newKey
                }

                override fun areContentsTheSame(
                    oldItemPosition: Int,
                    newItemPosition: Int
                ): Boolean {
                    val oldContent = contentSparseArray[oldItemPosition] ?: run {
                        val content = oldData[oldItemPosition].content()
                        contentSparseArray.put(oldItemPosition, content)
                        content
                    }
                    val newContent = newData[newItemPosition].content()
                    if (oldContent == null || newContent == null) {
                        if (BuildConfig.DEBUG && log) {
                            Log.d(
                                TAG, "content are not same," +
                                        " oldContent: ${oldContent}, newContent: $newContent"
                            )
                        }
                        return false
                    }
                    if (oldContent.size != newContent.size) {
                        if (BuildConfig.DEBUG && log) {
                            Log.d(
                                TAG, "content sizes are not equals," +
                                        " old: ${oldContent.size}, new: ${newContent.size}"
                            )
                        }
                        return false
                    }

                    oldContent.forEachIndexed { index, oldContentItem ->
                        val newContentItem = newContent[index]
                        if (oldContentItem.value == null && newContentItem.value == null) {
                            return@forEachIndexed
                        }
                        if (oldContentItem.value == null || newContentItem.value == null) {
                            if (BuildConfig.DEBUG && log) {
                                Log.d(
                                    TAG, "content are not same" +
                                            ", old: ${oldContentItem.value}" +
                                            ", new: ${newContentItem.value}"
                                )
                            }
                            return false
                        }
                        if (oldContentItem.value::class != newContentItem.value::class) {
                            if (BuildConfig.DEBUG && log) {
                                Log.d(
                                    TAG, "content are not same" +
                                            ", old class: ${oldContentItem.value::class}" +
                                            ", new class: ${newContentItem.value::class}"
                                )
                            }
                            return false
                        }
                        val oldComparatorResult = oldContentItem.comparator?.compare(
                            oldContentItem.value,
                            newContentItem.value
                        )
                        val newComparatorResult = newContentItem.comparator?.compare(
                            oldContentItem.value,
                            newContentItem.value
                        )
                        if (oldComparatorResult != null && newComparatorResult != null) {
                            if (oldComparatorResult != newComparatorResult) {
                                Log.w(
                                    "SimpleAdapter",
                                    "item ${oldData[oldItemPosition].key()}前后两次对比结果不同"
                                )
                                return false
                            } else {
                                val result = oldComparatorResult && newComparatorResult
                                if (result) return@forEachIndexed else {
                                    if (BuildConfig.DEBUG && log) {
                                        Log.d(
                                            TAG, "content are not same" +
                                                    ", old: ${oldContentItem.value}" +
                                                    ", new: ${newContentItem.value}" +
                                                    ", custom comparators are not null"
                                        )
                                    }
                                    return false
                                }
                            }
                        } else if (oldComparatorResult == null && newComparatorResult == null) {
                            val result = oldContentItem.value == newContentItem.value
                            if (result) return@forEachIndexed else {
                                if (BuildConfig.DEBUG && log) {
                                    Log.d(
                                        TAG, "content are not same" +
                                                ", old: ${oldContentItem.value}" +
                                                ", new: ${newContentItem.value}" +
                                                ", default comparator"
                                    )
                                }
                                return false
                            }
                        } else {
                            val result =
                                (oldComparatorResult ?: true) && (newComparatorResult ?: true)
                            if (result) return@forEachIndexed else {
                                if (BuildConfig.DEBUG && log) {
                                    Log.d(
                                        TAG, "content are not same" +
                                                ", old: ${oldContentItem.value}" +
                                                ", new: ${newContentItem.value}"
                                    )
                                }
                                return false
                            }
                        }
                    }

                    return true
                }
            })
        }

        withContext(Dispatchers.Main) {
            data = newData
            diffResult.dispatchUpdatesTo(object : ListUpdateCallback {
                override fun onInserted(position: Int, count: Int) {
                    this@SimpleAdapter.notifyItemRangeInserted(position, count)
                }

                override fun onRemoved(position: Int, count: Int) {
                    this@SimpleAdapter.notifyItemRangeRemoved(position, count)
                }

                override fun onMoved(fromPosition: Int, toPosition: Int) {
                    this@SimpleAdapter.notifyItemMoved(fromPosition, toPosition)
                    val layoutManager = recyclerView?.layoutManager as? LinearLayoutManager
                    if (layoutManager != null) {
                        val firstPosition = layoutManager.findFirstVisibleItemPosition()
                        if (fromPosition == firstPosition) {
                            recyclerView?.scrollToPosition(fromPosition)
                            return
                        }
                        if (toPosition <= firstPosition) {
                            recyclerView?.scrollToPosition(firstPosition)
                        }
                    }
                }

                override fun onChanged(position: Int, count: Int, payload: Any?) {
                    this@SimpleAdapter.notifyItemRangeChanged(position, count, payload)
                }

            })
            updateCallback?.invoke()
        }
    }

    class ItemViewHolder<T>(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) where T : ItemViewModel, T : ItemBinder {
        // 因为viewHolder可能会复用，因此这里记下最后一次绑定的itemData，以便在detach或者recycle的时候调用该
        // itemData的detach()或recycle()方法
        var itemData: T? = null
    }
}