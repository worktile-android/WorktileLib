package com.worktile.ui.recyclerview

import android.view.View
import androidx.lifecycle.LiveData
import com.worktile.base.arch.viewmodel.BaseViewModel
import java.lang.reflect.Field
import java.util.*

interface ItemBinder {
    fun viewCreator(): ViewCreator
    fun bind(itemView: View)
    fun type(): Any
}

interface ItemViewModel : BaseViewModel {
    fun key(): String
    fun content(): Array<ContentItem<*>>?
}

data class ContentItem<T>(
    val value: T,
    val comparator: ((o1: @UnsafeVariance T, o2: @UnsafeVariance T) -> Boolean)? = null
)

infix fun <T> T.withComparator(comparator: (o1: T, o2: T) -> Boolean) = ContentItem(this, comparator)

interface NoKeyItemViewModel : ItemViewModel {
    override fun key() = UUID.randomUUID().toString()
    override fun content(): Array<ContentItem<*>>? = null
}

interface DiffItemViewModel : ItemViewModel {
    override fun content(): Array<ContentItem<*>>? {
        val contentList = mutableListOf<ContentItem<*>>()
        val fields = mutableListOf<Field>()
        var clazz: Class<*>? = this::class.java
        while (clazz != null) {
            fields.addAll(clazz.declaredFields)
            clazz = clazz.superclass
        }
        fields
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

interface Definition : ItemViewModel, ItemBinder

