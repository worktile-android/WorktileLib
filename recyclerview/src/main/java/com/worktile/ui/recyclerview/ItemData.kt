package com.worktile.ui.recyclerview

import android.view.View
import androidx.lifecycle.LiveData
import com.worktile.common.arch.viewmodel.Default
import java.lang.reflect.Field
import java.util.*

interface ItemBinder {
    fun viewCreator(): ViewCreator
    fun bind(itemView: View)
    fun type(): Any = this::class
}

interface ItemViewModel {
    fun key(): Any
    fun content(): Array<ContentItem<*>>?
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
                val key = UUID.randomUUID()
                override fun key() = key
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

