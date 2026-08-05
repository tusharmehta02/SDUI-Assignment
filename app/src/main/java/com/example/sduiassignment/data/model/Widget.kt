package com.example.sduiassignment.data.model

import com.google.gson.annotations.SerializedName

object WidgetType {
    const val SEARCH_BAR = "SearchBarWidget"
    const val TAB_BAR = "TabBarWidget"
    const val HORIZONTAL_RAIL = "HorizontalRailWidget"
    const val CATEGORY_GRID = "CategoryGridWidget"
    const val BANNER_CAROUSEL = "BannerCarouselWidget"
    const val PRODUCT_COLLECTION = "ProductCollectionWidget"
}

object SublayoutType {
    const val HEADER = "HEADER"
    const val MAIN_SCREEN = "MAIN_SCREEN"
}

data class Widget(
    val widgetId: String,
    val widgetType: String,
    val status: String,
    val position: Int,
    val payload: WidgetPayload
)

data class WidgetGroup(
    val widgets: List<Widget>
)

data class Sublayout(
    val type: String,
    /** Only meaningful for MAIN_SCREEN sublayouts: which TabBarWidget item this content
     * set belongs to. Absent/"all" is the default content shown before any tab is tapped. */
    val tabId: String? = null,
    val widgetgroups: List<WidgetGroup>
)

data class DiscoverData(
    @SerializedName("request_id") val requestId: String?,
    @SerializedName("layout_id") val layoutId: String?,
    val sublayouts: List<Sublayout>,
    @SerializedName("page_offset") val pageOffset: String? = null,
    @SerializedName("pending_id") val pendingId: String? = null
)

data class DiscoverPageResponse(
    @SerializedName("request_type") val requestType: String?,
    val data: DiscoverData
)
