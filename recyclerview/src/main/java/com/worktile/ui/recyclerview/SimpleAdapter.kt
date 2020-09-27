package com.worktile.ui.recyclerview

import android.util.Log
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

typealias ViewCreator = (parent: ViewGroup) -> View

class SimpleAdapter<T>(
    val data: MutableList<T>,
    private val lifecycleOwner: LifecycleOwner
) : RecyclerView.Adapter<ItemViewHolder>(), LifecycleObserver where T : ItemViewModel, T : ItemBinder {
    private val typeToAdapterTypeMap = hashMapOf<Any, Int>()
    private val adapterTypeToViewCreatorMap = hashMapOf<Int, ViewCreator>()
    private var typeIndex = 0
    private var contentSparseArray = SparseArray<Array<ContentItem<*>>?>()
    var isLoadingMore: Boolean = false
    private val diffThreadExecutor = Executors.newSingleThreadExecutor()

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onLifecycleOwnerDestroy() {
        diffThreadExecutor.shutdown()
    }

    override fun getItemViewType(position: Int): Int {
        val itemData = data[position]
        typeToAdapterTypeMap[itemData.type()]?.run {
            return this
        } ?: run {
            val adapterType = typeIndex
            typeToAdapterTypeMap[itemData.type()] = adapterType
            adapterTypeToViewCreatorMap[adapterType] = itemData.viewCreator()
            typeIndex++
            return adapterType
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val viewCreator = adapterTypeToViewCreatorMap[viewType]!!
        return ItemViewHolder(viewCreator.invoke(parent))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        contentSparseArray.put(position, data[position].content())
        data[position].bind(holder.itemView)
    }

    override fun getItemCount(): Int {
        val size = data.size
        if (contentSparseArray.size() > size + 10) {
            contentSparseArray.removeAtRange(size, contentSparseArray.size() - size)
        }
        return size
    }

    fun updateData(prepareNewData: () -> List<T>, updateListener: (() -> Unit)? = null) {
        diffThreadExecutor.execute {
            runBlocking {
                val newData = prepareNewData.invoke()
                val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun getOldListSize() = data.size

                    override fun getNewListSize() = newData.size

                    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                        return data[oldItemPosition].key() == newData[newItemPosition].key()
                    }

                    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                        val oldContent = contentSparseArray[oldItemPosition]
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

                withContext(Dispatchers.Main) {
                    data.clear()
                    data.addAll(newData)
                    diffResult.dispatchUpdatesTo(this@SimpleAdapter)
                    updateListener?.invoke()
                }
            }
        }
    }
}

class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)