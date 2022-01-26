package com.worktile.lib

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.worktile.common.default
import com.worktile.ui.kanban.adapter.KanbanPagerAdapter
import com.worktile.ui.recyclerview.ItemDefinition
import com.worktile.ui.recyclerview.RecyclerViewViewModel
import com.worktile.ui.recyclerview.ViewCreator
//import com.worktile.ui.recyclerview.binder.bind
//import com.worktile.ui.recyclerview.livedata.extension.notifyChanged
//import com.worktile.ui.recyclerview.viewmodels.RecyclerViewViewModel
import kotlinx.android.synthetic.main.activity_kanban.*
import kotlinx.android.synthetic.main.fragment_kanban_page.*

class KanbanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kanban)
        kanban.pagerAdapter = PageAdapter(this)
    }
}

class PageAdapter(activity: FragmentActivity) : KanbanPagerAdapter(activity) {
    private val colors = listOf(Color.YELLOW, Color.BLUE, Color.CYAN, Color.DKGRAY, Color.GRAY, Color.GREEN, Color.LTGRAY, Color.MAGENTA, Color.WHITE, Color.RED)

    override fun getItemCount() = 10

    override fun newFragment(position: Int): Fragment {
        println("createFragment, position: $position")
        return PageFragment().apply {
            this.position = position
            color = colors[position]
        }
    }
}

class PageFragment : Fragment(R.layout.fragment_kanban_page) {
    internal var position: Int = 0
    internal var color: Int = Color.RED

    private val viewModel: PageFragmentViewModel by lazy {
        ViewModelProvider(requireActivity()).get("$position", PageFragmentViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        println("createView, position: $position")
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layout.setBackgroundColor(color)
//        recycler_view.bind(viewModel, this)
    }
}

class PageFragmentViewModel : RecyclerViewViewModel by default(), ViewModel() {
    private val data = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10" ,"16", "17", "18", "11", "101",
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" ,"16", "17", "18", "11", "101")

//    init {
//        recyclerViewData.run {
//            data.forEach { s ->
//                add(object : Item, DiffItemViewModel by default() {
//                    override val text = s
//                })
//            }
//        }
//        recyclerViewData.notifyChanged()
//    }
}

interface Item : ItemDefinition {
    val text: String

    override fun viewCreator(): ViewCreator = {
        LayoutInflater.from(it.context).inflate(R.layout.item_kanban_item, it, false)
    }

    override fun bind(itemView: View) {
        itemView.setOnClickListener {
            println("itemClick")
        }
        itemView.findViewById<TextView>(R.id.text_view).text = text
    }

    override fun type() = Item::class
}