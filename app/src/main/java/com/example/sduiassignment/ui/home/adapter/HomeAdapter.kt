package com.example.sduiassignment.ui.home.adapter

import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.sduiassignment.data.model.Widget
import com.example.sduiassignment.data.model.WidgetPayload
import com.example.sduiassignment.data.model.WidgetType
import com.example.sduiassignment.databinding.ItemWidgetBannerCarouselBinding
import com.example.sduiassignment.databinding.ItemWidgetCategoryGridBinding
import com.example.sduiassignment.databinding.ItemWidgetHorizontalRailBinding
import com.example.sduiassignment.databinding.ItemWidgetProductCollectionBinding
import com.example.sduiassignment.databinding.ItemWidgetSearchBarBinding
import com.example.sduiassignment.databinding.ItemWidgetTabBarBinding
import com.example.sduiassignment.ui.common.ActionHandler
import com.example.sduiassignment.ui.common.parseHexColorOrNull

private const val VIEW_TYPE_SEARCH_BAR = 0
private const val VIEW_TYPE_TAB_BAR = 1
private const val VIEW_TYPE_HORIZONTAL_RAIL = 2
private const val VIEW_TYPE_CATEGORY_GRID = 3
private const val VIEW_TYPE_BANNER_CAROUSEL = 4
private const val VIEW_TYPE_PRODUCT_COLLECTION = 5

class HomeAdapter(
    private val widgets: List<Widget>,
    private val onTabSelected: (WidgetPayload.TabBar.TabItem) -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int = when (widgets[position].widgetType) {
        WidgetType.SEARCH_BAR -> VIEW_TYPE_SEARCH_BAR
        WidgetType.TAB_BAR -> VIEW_TYPE_TAB_BAR
        WidgetType.HORIZONTAL_RAIL -> VIEW_TYPE_HORIZONTAL_RAIL
        WidgetType.CATEGORY_GRID -> VIEW_TYPE_CATEGORY_GRID
        WidgetType.BANNER_CAROUSEL -> VIEW_TYPE_BANNER_CAROUSEL
        WidgetType.PRODUCT_COLLECTION -> VIEW_TYPE_PRODUCT_COLLECTION
        else -> throw IllegalStateException("Unsupported widget type: ${widgets[position].widgetType}")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SEARCH_BAR ->
                SearchBarViewHolder(ItemWidgetSearchBarBinding.inflate(inflater, parent, false))
            VIEW_TYPE_TAB_BAR ->
                TabBarViewHolder(ItemWidgetTabBarBinding.inflate(inflater, parent, false))
            VIEW_TYPE_HORIZONTAL_RAIL ->
                HorizontalRailViewHolder(ItemWidgetHorizontalRailBinding.inflate(inflater, parent, false))
            VIEW_TYPE_CATEGORY_GRID ->
                CategoryGridViewHolder(ItemWidgetCategoryGridBinding.inflate(inflater, parent, false))
            VIEW_TYPE_BANNER_CAROUSEL ->
                BannerCarouselViewHolder(ItemWidgetBannerCarouselBinding.inflate(inflater, parent, false))
            VIEW_TYPE_PRODUCT_COLLECTION ->
                ProductCollectionViewHolder(ItemWidgetProductCollectionBinding.inflate(inflater, parent, false))
            else -> throw IllegalStateException("Unsupported view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val widget = widgets[position]
        when (holder) {
            is SearchBarViewHolder -> holder.bind(widget.payload as WidgetPayload.SearchBar)
            is TabBarViewHolder -> holder.bind(widget.payload as WidgetPayload.TabBar, onTabSelected)
            is HorizontalRailViewHolder -> holder.bind(widget.payload as WidgetPayload.HorizontalRail)
            is CategoryGridViewHolder -> holder.bind(widget.payload as WidgetPayload.CategoryGrid)
            is BannerCarouselViewHolder -> holder.bind(widget.payload as WidgetPayload.BannerCarousel)
            is ProductCollectionViewHolder -> holder.bind(widget.payload as WidgetPayload.ProductCollection)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        when (holder) {
            is BannerCarouselViewHolder -> holder.stopAutoScroll()
            is SearchBarViewHolder -> holder.stopHintRotation()
        }
    }

    override fun getItemCount(): Int = widgets.size

    // ---- ViewHolders ----

    class SearchBarViewHolder(
        private val binding: ItemWidgetSearchBarBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val hintHandler = Handler(Looper.getMainLooper())
        private var hintRunnable: Runnable? = null

        fun bind(payload: WidgetPayload.SearchBar) {
            stopHintRotation()
            binding.root.setOnClickListener {
                ActionHandler.handle(binding.root.context, payload.action, "Search")
            }

            val hints = payload.hintChips.orEmpty().map { "Search $it" }
            if (hints.isEmpty()) {
                binding.tvSearchPlaceholder.text = payload.placeholderText.orEmpty()
                return
            }
            if (hints.size == 1) {
                binding.tvSearchPlaceholder.text = hints.first()
                return
            }
            startHintRotation(hints)
        }

        private fun startHintRotation(hints: List<String>) {
            var index = 0
            binding.tvSearchPlaceholder.text = hints[index]
            val runnable = object : Runnable {
                override fun run() {
                    index = (index + 1) % hints.size
                    binding.tvSearchPlaceholder.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction {
                            binding.tvSearchPlaceholder.text = hints[index]
                            binding.tvSearchPlaceholder.animate().alpha(1f).setDuration(200).start()
                        }
                        .start()
                    hintHandler.postDelayed(this, 2200)
                }
            }
            hintRunnable = runnable
            hintHandler.postDelayed(runnable, 2200)
        }

        fun stopHintRotation() {
            hintRunnable?.let { hintHandler.removeCallbacks(it) }
            hintRunnable = null
            binding.tvSearchPlaceholder.animate().cancel()
            binding.tvSearchPlaceholder.alpha = 1f
        }
    }

    class TabBarViewHolder(
        private val binding: ItemWidgetTabBarBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(payload: WidgetPayload.TabBar, onTabSelected: (WidgetPayload.TabBar.TabItem) -> Unit) {
            binding.rvTabBarItems.layoutManager =
                LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
            binding.rvTabBarItems.adapter = TabBarItemAdapter(payload.items, onTabSelected)
        }
    }

    class HorizontalRailViewHolder(
        private val binding: ItemWidgetHorizontalRailBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(payload: WidgetPayload.HorizontalRail) {
            binding.tvRailTitle.text = payload.header?.title?.text.orEmpty()
            val badge = payload.header?.badge
            binding.tvRailBadge.text = badge?.text.orEmpty()
            binding.tvRailBadge.visibility = if (badge?.text.isNullOrBlank()) View.GONE else View.VISIBLE

            parseHexColorOrNull(badge?.backgroundColor)?.let { color ->
                (binding.tvRailBadge.background.mutate() as? GradientDrawable)?.setColor(color)
            }
            parseHexColorOrNull(badge?.textColor)?.let { binding.tvRailBadge.setTextColor(it) }

            binding.rvRailItems.layoutManager =
                LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
            binding.rvRailItems.adapter = RailItemAdapter(payload.items)
        }
    }

    class CategoryGridViewHolder(
        private val binding: ItemWidgetCategoryGridBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(payload: WidgetPayload.CategoryGrid) {
            binding.tvGridTitle.text = payload.header?.title?.text.orEmpty()
            val columnCount = (payload.gridConfig?.columnCount ?: 3).coerceAtLeast(1)
            binding.rvGridItems.layoutManager = GridLayoutManager(binding.root.context, columnCount)
            binding.rvGridItems.adapter = GridItemAdapter(payload.items, payload.gridConfig?.cardStyle)
        }
    }

    class BannerCarouselViewHolder(
        private val binding: ItemWidgetBannerCarouselBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val autoScrollHandler = Handler(Looper.getMainLooper())
        private var autoScrollRunnable: Runnable? = null

        fun bind(payload: WidgetPayload.BannerCarousel) {
            stopAutoScroll()
            binding.vpBanner.adapter = BannerPagerAdapter(payload.items)
            setupDots(payload.items.size)

            binding.vpBanner.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    updateDots(position)
                }
            })

            val config = payload.carousel
            if (config?.autoScrollEnabled == true && payload.items.size > 1) {
                startAutoScroll(payload.items.size, config.autoScrollIntervalMs ?: 3000L)
            }
        }

        private fun setupDots(count: Int) {
            binding.llBannerDots.removeAllViews()
            if (count <= 1) return
            repeat(count) { index ->
                val dot = View(binding.root.context)
                val size = (8 * binding.root.resources.displayMetrics.density).toInt()
                val margin = (4 * binding.root.resources.displayMetrics.density).toInt()
                val params = android.widget.LinearLayout.LayoutParams(size, size)
                params.marginStart = margin
                params.marginEnd = margin
                dot.layoutParams = params
                dot.setBackgroundResource(
                    if (index == 0) com.example.sduiassignment.R.drawable.dot_active
                    else com.example.sduiassignment.R.drawable.dot_inactive
                )
                binding.llBannerDots.addView(dot)
            }
        }

        private fun updateDots(activeIndex: Int) {
            for (i in 0 until binding.llBannerDots.childCount) {
                binding.llBannerDots.getChildAt(i).setBackgroundResource(
                    if (i == activeIndex) com.example.sduiassignment.R.drawable.dot_active
                    else com.example.sduiassignment.R.drawable.dot_inactive
                )
            }
        }

        private fun startAutoScroll(count: Int, intervalMs: Long) {
            val runnable = object : Runnable {
                override fun run() {
                    val next = (binding.vpBanner.currentItem + 1) % count
                    binding.vpBanner.setCurrentItem(next, true)
                    autoScrollHandler.postDelayed(this, intervalMs)
                }
            }
            autoScrollRunnable = runnable
            autoScrollHandler.postDelayed(runnable, intervalMs)
        }

        fun stopAutoScroll() {
            autoScrollRunnable?.let { autoScrollHandler.removeCallbacks(it) }
            autoScrollRunnable = null
        }
    }

    class ProductCollectionViewHolder(
        private val binding: ItemWidgetProductCollectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(payload: WidgetPayload.ProductCollection) {
            binding.tvProductsTitle.text = payload.header?.title?.text.orEmpty()
            val subtitle = payload.header?.subtitle?.text
            binding.tvProductsSubtitle.text = subtitle.orEmpty()
            binding.tvProductsSubtitle.visibility = if (subtitle.isNullOrBlank()) View.GONE else View.VISIBLE
            val ctaLabel = payload.header?.cta?.label
            binding.tvProductsCta.text = ctaLabel.orEmpty()
            binding.tvProductsCta.visibility = if (ctaLabel.isNullOrBlank()) View.GONE else View.VISIBLE
            binding.tvProductsCta.setOnClickListener {
                ActionHandler.handle(binding.root.context, payload.header?.cta?.action, ctaLabel)
            }

            val chips = payload.filterChips.orEmpty()
            binding.rvFilterChips.visibility = if (chips.isEmpty()) View.GONE else View.VISIBLE
            if (chips.isNotEmpty()) {
                binding.rvFilterChips.layoutManager =
                    LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
                binding.rvFilterChips.adapter = FilterChipAdapter(chips)
            }

            binding.rvProducts.layoutManager =
                LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
            binding.rvProducts.adapter = ProductCardAdapter(payload.items)
        }
    }
}
