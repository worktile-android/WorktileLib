package com.worktile.lib

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.ViewModel
import com.worktile.common.arch.viewmodel.default
import com.worktile.ui.recyclerview.Definition
import com.worktile.ui.recyclerview.DiffItemViewModel
import com.worktile.ui.recyclerview.binder.bind
import com.worktile.ui.recyclerview.decoration.LineDecoration
import com.worktile.ui.recyclerview.viewmodels.RecyclerViewViewModel
import kotlinx.android.synthetic.main.activity_demo.*
import kotlinx.android.synthetic.main.item_category_item.view.*
import kotlinx.android.synthetic.main.item_project_item.view.*
import kotlin.random.Random

class DemoActivity : AppCompatActivity() {

    private val viewModel: DemoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo)
        recyclerView.bind(viewModel, this)
        recyclerView.addItemDecoration(LineDecoration())
    }
}

class DemoViewModel : ViewModel(), RecyclerViewViewModel by default() {
    private val colorList = arrayOf("#B0F566", "#5CC9F5", "#65D4BC", "#25D0B2", "#FECD3E")
    private val projectTitles = arrayOf("看板协作", "全部组件", "工时测试", "组件A", "001项目")
    private val srcList = arrayOf(
        R.mipmap.icon_project_public,
        R.mipmap.icon_project_private
    )

    init {
        recyclerViewData.value?.run {
            for (item in 0..30) {
                when (item) {
                    0 -> add(CategoryItemViewModel(item, "工作"))
                    1 -> add(ProjectItemViewModel(item, "统计报表", R.mipmap.icon_statistic_data))
                    2 -> add(ProjectItemViewModel(item, "人员视图", R.mipmap.icon_work_member))
                    3 -> add(
                        ProjectItemViewModel(
                            item,
                            "我的任务",
                            R.mipmap.icon_project_main_my_tasks
                        )
                    )
                    4 -> add(CategoryItemViewModel(item, "置顶"))
                    9 -> add(CategoryItemViewModel(item, "项目"))
                    else -> {
                        val randomIndex = Random.nextInt(4)
                        val randomSrcIndex = Random.nextInt(2)
                        add(
                            ProjectItemViewModel(
                                item,
                                projectTitles[randomIndex],
                                srcList[randomSrcIndex],
                                colorList[randomIndex]
                            )
                        )
                    }
                }
            }
        }
    }

    class CategoryItemViewModel(private val key: Int, private val title: String) :
        DiffItemViewModel,
        Definition {

        override fun key() = key

        override fun viewCreator() = { parent: ViewGroup ->
            LayoutInflater.from(parent.context).inflate(R.layout.item_category_item, parent, false)
        }

        override fun bind(itemView: View) {
            itemView.categoryTitle.text = title
        }

        override fun type() = R.layout.item_category_item
    }

    class ProjectItemViewModel(
        private val key: Int,
        private val title: String,
        private val src: Int,
        private val color: String? = null
    ) : DiffItemViewModel,
        Definition {
        override fun key() = key

        override fun viewCreator() = { parent: ViewGroup ->
            LayoutInflater.from(parent.context).inflate(R.layout.item_project_item, parent, false)
        }

        override fun bind(itemView: View) {
            itemView.projectName.text = title
            itemView.projectIcon.run {
                setImageResource(src)
                color?.also {
                    setImageDrawable(drawable.tintDrawable(Color.parseColor(it)))
                }

            }
        }

        override fun type() = R.layout.item_project_item
    }
}

fun Drawable.tintDrawable(color: Int): Drawable = DrawableCompat.wrap(this).apply {
    DrawableCompat.setTint(this, color)
}

