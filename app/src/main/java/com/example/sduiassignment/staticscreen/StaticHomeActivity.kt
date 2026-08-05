package com.example.sduiassignment.staticscreen

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sduiassignment.R
import com.example.sduiassignment.data.repository.DEFAULT_TAB_ID
import com.example.sduiassignment.databinding.ActivityMainBinding
import com.example.sduiassignment.ui.common.PerfTrace
import com.example.sduiassignment.ui.home.adapter.HomeAdapter

/**
 * PERF.md baseline: hardcoded counterpart to MainActivity. Same layout, same HomeAdapter,
 * same ViewHolders - the only difference is where the widget list comes from. Here it's
 * StaticHomeData.build(), a compile-time Kotlin object graph; there it's a network fetch
 * through Retrofit/Gson/WidgetDeserializer. No ViewModel/Hilt/coroutines needed: there is
 * nothing asynchronous on this path.
 */
class StaticHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        PerfTrace.mark("static", "activity_start")
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.doOnPreDraw { PerfTrace.mark("static", "first_frame") }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.rvHeader.layoutManager = LinearLayoutManager(this)
        binding.rvHome.layoutManager = LinearLayoutManager(this)
        binding.progressBar.visibility = View.GONE
        binding.errorLayout.visibility = View.GONE
        binding.swipeRefresh.visibility = View.VISIBLE
        binding.swipeRefresh.isEnabled = false
        binding.headerContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.surface_card))

        val widgets = StaticHomeData.build()
        PerfTrace.mark("static", "adapter_set")
        binding.rvHeader.adapter = HomeAdapter(widgets.headerWidgets)
        binding.rvHome.adapter = HomeAdapter(widgets.contentFor(DEFAULT_TAB_ID), perfTag = "static")
        binding.root.doOnPreDraw { PerfTrace.mark("static", "content_first_frame") }
    }
}
