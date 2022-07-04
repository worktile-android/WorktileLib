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

interface ItemDefinition {
    fun viewCreator(): ViewCreator
    fun bind(itemView: View)
    fun attach(itemView: View) { }
    fun detach(itemView: View) { }
    fun recycle(itemView: View) { }
    fun itemAnimationFinished(itemView: View) { }
    fun allAnimationsFinished(itemView: View) { }
    fun type(): Any = this::class
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

inline fun <reified T> contentArrayOf(vararg elements: T): Array<ContentItem<T>> {
    return arrayOf(*elements).map { ContentItem(it) }.toTypedArray()
}

