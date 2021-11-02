package com.worktile.ui.recyclerview

import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEachIndexed
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.worktile.common.Default
import java.lang.reflect.Field
import java.util.*

typealias ViewCreator = (parent: ViewGroup) -> View

interface ItemBinder {
    fun viewCreator(): ViewCreator
    fun bind(itemView: View)
    fun recycle(itemView: View) { }
    fun type(): Any = this::class
}

interface ItemViewModel {
    fun key(): Any
    fun content(): Array<ContentItem<*>>?
}

abstract class StateBinder : ItemBinder {
    var recyclerView: RecyclerView? = null

    fun itemView(): View? {
        return recyclerView?.run {
            (adapter as? SimpleAdapter<*>)?.run {
                val index = data.run data@{
                    forEachIndexed { index: Int, item: ItemViewModel ->
                        if (item == this@StateBinder) {
                            return@data index
                        }
                    }
                    -1
                }
                findViewHolderForAdapterPosition(index)?.itemView
            }
        }
    }
}

fun interface ContentItemComparator<T> {
    fun compare(o1: @UnsafeVariance T, o2: @UnsafeVariance T): Boolean
}

data class ContentItem<T>(
    val value: T,
    val comparator: (ContentItemComparator<T>)? = null
)

infix fun <T> T.withComparator(comparator: ContentItemComparator<T>) = ContentItem(this, comparator)

interface DiffLiveDataItemViewModel : ItemViewModel {
    override fun content(): Array<ContentItem<*>>? {
        val contentList = mutableListOf<ContentItem<*>>()
        getAllFields(this)
            .filter {
                LiveData::class.java.isAssignableFrom(it.type)
            }
            .forEach {
                it.isAccessible = true
                val liveData = it.get(this) as? LiveData<*>
                contentList.add(ContentItem(liveData?.value))
            }
        return contentList.toTypedArray()
    }
}

interface DiffItemViewModel : ItemViewModel {
    override fun content(): Array<ContentItem<*>>? {
        val contentList = mutableListOf<ContentItem<*>>()
        getAllFields(this)
            .filter { !it.name.endsWith("_skip") }
            .forEach {
                it.isAccessible = true
                contentList.add(ContentItem(it.get(this)))
            }
        return contentList.toTypedArray()
    }

    companion object {
        @Default
        fun create(): DiffItemViewModel {
            return object : DiffItemViewModel {
                val key_skip = UUID.randomUUID()
                override fun key() = key_skip
            }
        }
    }
}

private fun getAllFields(obj: Any): List<Field> {
    val fields = mutableListOf<Field>()
    var clazz: Class<*>? = obj::class.java
    while (clazz != null) {
        fields.addAll(clazz.declaredFields)
        clazz = clazz.superclass
    }
    return fields
}

interface ItemDefinition : ItemViewModel, ItemBinder

