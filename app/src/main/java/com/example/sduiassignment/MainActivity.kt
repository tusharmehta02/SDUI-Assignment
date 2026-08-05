package com.example.sduiassignment

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sduiassignment.data.model.WidgetPayload
import com.example.sduiassignment.data.repository.DEFAULT_TAB_ID
import com.example.sduiassignment.data.repository.HomeWidgets
import com.example.sduiassignment.databinding.ActivityMainBinding
import com.example.sduiassignment.ui.common.PerfTrace
import com.example.sduiassignment.ui.common.parseHexColorOrNull
import com.example.sduiassignment.ui.home.HomeUiState
import com.example.sduiassignment.ui.home.HomeViewModel
import com.example.sduiassignment.ui.home.adapter.HomeAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: HomeViewModel by viewModels()

    private var homeWidgets: HomeWidgets? = null
    private var currentHeaderColor = 0
    private var headerColorAnimator: ValueAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        PerfTrace.mark("sdui", "activity_start")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.doOnPreDraw { PerfTrace.mark("sdui", "first_frame") }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        currentHeaderColor = ContextCompat.getColor(this, R.color.surface_card)

        binding.rvHeader.layoutManager = LinearLayoutManager(this)
        binding.rvHome.layoutManager = LinearLayoutManager(this)
        binding.btnRetry.setOnClickListener { viewModel.loadHome() }
        binding.swipeRefresh.setOnRefreshListener { viewModel.loadHome() }

        observeUiState()
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: HomeUiState) {
        binding.swipeRefresh.isRefreshing = false
        when (state) {
            is HomeUiState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.errorLayout.visibility = View.GONE
                binding.swipeRefresh.visibility = View.INVISIBLE
            }
            is HomeUiState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.errorLayout.visibility = View.GONE
                binding.swipeRefresh.visibility = View.VISIBLE
                homeWidgets = state.widgets
                PerfTrace.mark("sdui", "adapter_set")
                binding.rvHeader.adapter = HomeAdapter(state.widgets.headerWidgets, ::onTabSelected)
                binding.rvHome.adapter = HomeAdapter(state.widgets.contentFor(DEFAULT_TAB_ID), perfTag = "sdui")
                binding.root.doOnPreDraw { PerfTrace.mark("sdui", "content_first_frame") }
            }
            is HomeUiState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.visibility = View.INVISIBLE
                binding.errorLayout.visibility = View.VISIBLE
                binding.tvError.text = state.message
            }
        }
    }

    /** Contract-driven: tapping a TabBarWidget item both re-tints the pinned header
     * (backgroundColor) and, if the tab has its own content set in the contract, swaps
     * the scrolling section to it (falling back to the default "all" content otherwise). */
    private fun onTabSelected(tab: WidgetPayload.TabBar.TabItem) {
        animateHeaderColor(tab.backgroundColor)

        val content = homeWidgets?.contentFor(tab.id) ?: return
        binding.rvHome.adapter = HomeAdapter(content, perfTag = "sdui")
        binding.rvHome.scrollToPosition(0)
    }

    private fun animateHeaderColor(hex: String?) {
        val targetColor = parseHexColorOrNull(hex) ?: ContextCompat.getColor(this, R.color.surface_card)
        if (targetColor == currentHeaderColor) return

        headerColorAnimator?.cancel()
        headerColorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), currentHeaderColor, targetColor).apply {
            duration = 250
            addUpdateListener { binding.headerContainer.setBackgroundColor(it.animatedValue as Int) }
            start()
        }
        currentHeaderColor = targetColor
    }
}
