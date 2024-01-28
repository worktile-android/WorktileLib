package com.worktile.ui.recyclerview

import android.view.View
import android.view.ViewGroup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.ConcurrentHashMap

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
    fun groupId(): String? = null
    // 会在onCreateViewHolder的时候创建，用来指定某个itemViewHolder是否可被回收，这里有区别于isRecyclable，因为
    // isReusable只会被调用一次，而不是setIsRecyclable需要床对调用，面对的场景是某些确定了不需要被回收的item，例如
    // 含有theia的item
    fun isReusable(): Boolean = true
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

internal val postponeAsyncMainBlocks = WeakHashMap<ItemDefinition, () -> Unit>()

fun ItemDefinition.itemAsync(async: suspend ItemAsyncScope.() -> Unit) {
    CoroutineScope(Dispatchers.Default).launch {
        async.invoke(ItemAsyncScope())
    }
}

class ItemAsyncScope {
    suspend fun ItemDefinition.main(itemView: View, block: () -> Unit) {
        withContext(Dispatchers.Main) {
            val keyInTag = itemView.getTag(R.id.item_definition_key)
            if (keyInTag == key() || keyInTag == null) {
                block()
            } else {
                postponeAsyncMainBlocks[this@main] = block
            }
        }
    }
}

