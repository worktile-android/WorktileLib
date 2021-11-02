package com.worktile.ui.recyclerview

import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.worktile.common.arch.viewmodel.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

const val TAG = "RecyclerView"

fun RecyclerView.executeAfterAllAnimationsAreFinished(
    callback: () -> Unit
) {
    Handler(Looper.getMainLooper()).post(object: Runnable {
        override fun run() {
            if (isAnimating) {
                itemAnimator?.isRunning {
                    Handler(Looper.getMainLooper()).post(this)
                }
                return
            }
            callback.invoke()
        }
    })
}

fun <T> RecyclerView.bind(
    viewModel: T,
    activity: ComponentActivity,
    config: Config = Config()
) where T : RecyclerViewViewModel, T : ViewModel {
    bind(viewModel, activity, activity, config)
}

fun <T> RecyclerView.bind(
    viewModel: T,
    fragment: Fragment,
    config: Config = Config()
) where T : RecyclerViewViewModel, T : ViewModel {
    bind(viewModel, fragment, fragment.viewLifecycleOwner, config)
}

private fun <T> RecyclerView.bind(
    viewModel: T,
    viewModelOwner: ViewModelStoreOwner,
    lifecycleOwner: LifecycleOwner,
    config: Config
) where T : RecyclerViewViewModel, T : ViewModel {
    val innerViewModel = viewModel(viewModelOwner) { InnerViewModel() }

    viewModel.apply {
        recyclerViewData.innerViewModel = innerViewModel
        loadingState.config = config
        loadingState.viewModel = viewModel
    }

    layoutManager = LinearLayoutManager(context)
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (!canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {

            }
        }
    })

    adapter = SimpleAdapter(
        innerViewModel.adapterData.value,
        lifecycleOwner
    ).apply {
        CoroutineScope(Dispatchers.Default).launch {
            innerViewModel
                .adapterData
                .collect {
                    updateData(it)
                }
        }
    }
}

internal class InnerViewModel : ViewModel() {
    val adapterData = MutableStateFlow(emptyList<ItemDefinition>())
}

class Config {
    var loadingViewCreator: ViewCreator? = null
    var emptyViewCreator: ViewCreator? = null
    var failureViewCreator: ViewCreator? = null

    var loadMoreOnFooter = true
    var footerLoadingViewCreator: ViewCreator? = null
    var footerNoMoreViewCreator: ViewCreator? = null
    var footerFailureViewCreator: ViewCreator? = null
}