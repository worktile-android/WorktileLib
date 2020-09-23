package com.worktile.ui.recyclerview

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.worktile.base.arch.viewmodel.BaseViewModel
import java.lang.reflect.Field
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.typeOf

/**
 * @param itemBinder 这里不推荐在可以复用的情况下使用lambda表达式，因为每个类型的itemBinder对应RecyclerView Adapter中的一个type
 */
data class ItemData<T : ItemViewModel>(
    val itemViewModel: T,
    val itemView: View,
    val itemBinder: ItemBinder<T>
)

fun interface ItemBinder<T : ItemViewModel> {
    /**
     * 该处不会出现类型错误的情况，参考[SimpleAdapter.onBindViewHolder]，itemBinder和itemViewHolder都被保存在同一个itemData中，
     * 因此取出的时候也不会出现类型不匹配的错误
     */
    fun bind(itemViewModel: @UnsafeVariance T, itemView: View)
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

abstract class NoKeyItemViewModel : ItemViewModel {
    override fun key() = UUID.randomUUID().toString()
    override fun content(): Array<ContentItem<*>>? = null
}

abstract class DiffItemViewModel : ItemViewModel {
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

