package com.example.sduiassignment.data.remote

import com.example.sduiassignment.data.model.DiscoverPageResponse
import com.example.sduiassignment.data.model.GridCardStyle
import com.example.sduiassignment.data.model.SublayoutType
import com.example.sduiassignment.data.model.Widget
import com.example.sduiassignment.data.model.WidgetPayload
import com.example.sduiassignment.data.model.WidgetType
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Parses the real contract hosted at https://api.npoint.io/a9f60712115da1ebeb93 (snapshot). */
class RetrofitParsingTest {

    private val gson = GsonBuilder()
        .registerTypeAdapter(Widget::class.java, WidgetDeserializer())
        .create()

    private fun parseResponse(): DiscoverPageResponse {
        val json = javaClass.classLoader!!.getResourceAsStream("hosted_home.json")!!
            .bufferedReader().readText()
        return gson.fromJson(json, DiscoverPageResponse::class.java)
    }

    @Test
    fun parsesAllEightHomeWidgets() {
        val response = parseResponse()
        val widgets = response.data.sublayouts.flatMap { it.widgetgroups }.flatMap { it.widgets }

        assertEquals(8, widgets.size)
        assertEquals(WidgetType.SEARCH_BAR, widgets[0].widgetType)
        assertTrue(widgets[0].payload is WidgetPayload.SearchBar)

        val searchBar = widgets[0].payload as WidgetPayload.SearchBar
        assertEquals("Search Mahindra cars", searchBar.placeholderText)

        val tabBar = widgets.first { it.widgetType == WidgetType.TAB_BAR }.payload
        assertTrue(tabBar is WidgetPayload.TabBar)
        val tabs = (tabBar as WidgetPayload.TabBar).items
        assertEquals(6, tabs.size)
        assertEquals("All", tabs[0].label)
        assertEquals(true, tabs[0].selected)
        assertEquals("Buy used car", tabs[1].label)
        assertEquals("buy_used_car", tabs[1].id)
        assertEquals("Sell car", tabs[2].label)

        val rail = widgets.first { it.widgetType == WidgetType.HORIZONTAL_RAIL }.payload
        assertTrue(rail is WidgetPayload.HorizontalRail)
        assertEquals(4, (rail as WidgetPayload.HorizontalRail).items.size)

        val grid = widgets.first { it.widgetType == WidgetType.CATEGORY_GRID }.payload
        assertTrue(grid is WidgetPayload.CategoryGrid)
        assertEquals(6, (grid as WidgetPayload.CategoryGrid).items.size)

        val banner = widgets.first { it.widgetType == WidgetType.BANNER_CAROUSEL }.payload
        assertTrue(banner is WidgetPayload.BannerCarousel)
        assertEquals(2, (banner as WidgetPayload.BannerCarousel).items.size)

        val products = widgets.first { it.widgetType == WidgetType.PRODUCT_COLLECTION }.payload
        assertTrue(products is WidgetPayload.ProductCollection)
        val productCollection = products as WidgetPayload.ProductCollection
        assertEquals(2, productCollection.items.size)
        assertEquals("2017 Maruti Swift", productCollection.items[0].title)
        assertEquals(465000.0, productCollection.items[0].price?.sellingPrice)
    }

    @Test
    fun groupsMainScreenContentByTabId() {
        val response = parseResponse()
        val mainScreenByTab = response.data.sublayouts
            .filter { it.type == SublayoutType.MAIN_SCREEN }
            .groupBy { it.tabId ?: "all" }

        assertEquals(setOf("all", "buy_used_car"), mainScreenByTab.keys)

        val buyUsedCarWidgets = mainScreenByTab.getValue("buy_used_car")
            .flatMap { it.widgetgroups }.flatMap { it.widgets }
        assertEquals(2, buyUsedCarWidgets.size)

        val grid = buyUsedCarWidgets.first { it.widgetType == WidgetType.CATEGORY_GRID }
            .payload as WidgetPayload.CategoryGrid
        assertEquals(GridCardStyle.IMAGE_CARD, grid.gridConfig?.cardStyle)
        assertEquals(2, grid.gridConfig?.columnCount)
        assertEquals(4, grid.items.size)
        assertEquals("All cars", grid.items[0].title)
        assertEquals("Cars at best price", grid.items[0].subtitle)

        val products = buyUsedCarWidgets.first { it.widgetType == WidgetType.PRODUCT_COLLECTION }
            .payload as WidgetPayload.ProductCollection
        assertEquals("Cars you'll love", products.header?.title?.text)
        assertEquals("Exclusive offers on used cars", products.header?.subtitle?.text)
    }
}
