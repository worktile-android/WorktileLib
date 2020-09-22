package com.worktile.ui.recyclerview

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
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

    fun updateData(newData: List<ItemData<out T>>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = data.size

            override fun getNewListSize() = newData.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                TODO("Not yet implemented")
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                TODO("Not yet implemented")
            }
        })
        data.clear()
        data.addAll(newData)
        diffResult.dispatchUpdatesTo(this)
    }
}

class ItemViewHolder<T : ItemViewModel>(val itemData: ItemData<out T>) : RecyclerView.ViewHolder(itemData.itemView)