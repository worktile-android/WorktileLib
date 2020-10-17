package com.worktile.lib

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.worktile.common.arch.livedata.notifyChanged
import com.worktile.common.arch.viewmodel.default
import com.worktile.ui.recyclerview.ItemDefinition
import com.worktile.ui.recyclerview.DiffItemViewModel
import com.worktile.ui.recyclerview.ViewCreator
import com.worktile.ui.recyclerview.binder.bind
import com.worktile.ui.recyclerview.viewmodels.RecyclerViewViewModel
import kotlinx.android.synthetic.main.fragment_kanban_page.*

class KanbanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kanban)
    }
}

class PageAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
//    private val colors = listOf(Color.YELLOW, Color.BLUE, Color.CYAN, Color.DKGRAY, Color.GRAY, Color.GREEN, Color.LTGRAY, Color.MAGENTA, Color.WHITE, Color.RED)

    override fun getItemCount() = 10

    override fun createFragment(position: Int): Fragment {
        return PageFragment().apply {
            this.position = position
        }
    }
}

class PageFragment : Fragment(R.layout.fragment_kanban_page) {
    companion object {
        private const val POSITION = "position"
    }

    internal var position: Int = 0
    private val viewModel: PageFragmentViewModel by lazy {
        ViewModelProvider(requireActivity()).get("$position", PageFragmentViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        position = savedInstanceState?.getInt(POSITION) ?: 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recycler_view.bind(viewModel, this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(POSITION, position)
    }
}

class PageFragmentViewModel : RecyclerViewViewModel by default(), ViewModel() {
    private val data = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10" ,"16", "17", "18", "11", "101",
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" ,"16", "17", "18", "11", "101")

    init {
        recyclerViewData.value?.run {
            data.forEach { s ->
                add(object : Item, DiffItemViewModel by default() {
                    override val text = s
                })
            }
        }
        recyclerViewData.notifyChanged()
    }
}

interface Item : ItemDefinition {
    val text: String

    override fun viewCreator(): ViewCreator = {
        TextView(it.context).apply {
            textSize = 30F
        }
    }

    override fun bind(itemView: View) {
        (itemView as TextView).text = text
    }

    override fun type() = Item::class
}