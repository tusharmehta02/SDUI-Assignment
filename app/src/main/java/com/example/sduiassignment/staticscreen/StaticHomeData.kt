package com.example.sduiassignment.staticscreen

import com.example.sduiassignment.data.model.ActionRef
import com.example.sduiassignment.data.model.BadgeRef
import com.example.sduiassignment.data.model.CtaRef
import com.example.sduiassignment.data.model.DiscoverPagePayload
import com.example.sduiassignment.data.model.DiscoverWidgetPayload
import com.example.sduiassignment.data.model.HeaderRef
import com.example.sduiassignment.data.model.ImageRef
import com.example.sduiassignment.data.model.TextRef
import com.example.sduiassignment.data.model.Widget
import com.example.sduiassignment.data.model.WidgetPayload
import com.example.sduiassignment.data.model.WidgetType
import com.example.sduiassignment.data.repository.DEFAULT_TAB_ID
import com.example.sduiassignment.data.repository.HomeWidgets

/**
 * PERF.md comparison baseline: the exact same home-screen content as the "all" tab of
 * sdui_discover_page.json, but as compile-time Kotlin object literals - no network call,
 * no JSON, no Gson reflection, no widget_type registry lookup by string. Fed into the same
 * HomeAdapter/ViewHolders/layouts as the SDUI path, so the only thing this isolates is the
 * fetch+parse+dispatch overhead the SDUI path pays and this one doesn't.
 */
object StaticHomeData {

    private fun navigate(page: String, navTarget: String, params: Map<String, String>? = null): ActionRef =
        ActionRef(
            type = "ACTION_TYPE_DISCOVER_PAGE",
            viewDomain = "VIEW_DOMAIN_HOME",
            discoverPage = DiscoverPagePayload(
                page = page,
                params = params,
                type = "DISCOVER_PAGE_ACTION_TYPE_NAVIGATION",
                navTarget = navTarget
            )
        )

    private fun refreshWidget(widgetId: String, params: Map<String, String>? = null): ActionRef =
        ActionRef(
            type = "ACTION_TYPE_DISCOVER_WIDGETS",
            discoverWidget = DiscoverWidgetPayload(widgetId, params)
        )

    private val searchBar = Widget(
        widgetId = "HPSearchBarWidget",
        widgetType = WidgetType.SEARCH_BAR,
        status = "SUCCESS",
        position = 0,
        payload = WidgetPayload.SearchBar(
            placeholderText = "Search Mahindra cars",
            hintChips = listOf("Alto", "Swift", "Seltos"),
            variant = "SEARCH_VIEW_TYPE_NEW",
            action = navigate("Search", "NAV_TARGET_SEARCH")
        )
    )

    private val tabBar = Widget(
        widgetId = "HPTabBarWidget",
        widgetType = WidgetType.TAB_BAR,
        status = "SUCCESS",
        position = 1,
        payload = WidgetPayload.TabBar(
            items = listOf(
                WidgetPayload.TabBar.TabItem(
                    id = "all", label = "All", selected = true, backgroundColor = "#EDEBFB",
                    action = refreshWidget("hp_layout_car_assignment", mapOf("filter-tab" to "all"))
                ),
                WidgetPayload.TabBar.TabItem(
                    id = "buy_used_car", label = "Buy used car", backgroundColor = "#E8F1FD",
                    action = navigate("categoryPage", "NAV_TARGET_LISTING", mapOf("filter-collectionId" to "used-cars", "pageTitle" to "Buy used car"))
                ),
                WidgetPayload.TabBar.TabItem(
                    id = "sell_car", label = "Sell car", backgroundColor = "#E6F7EC",
                    action = navigate("SellCar", "NAV_TARGET_SELL_CAR")
                ),
                WidgetPayload.TabBar.TabItem(
                    id = "loans", label = "Loans", backgroundColor = "#FFF4E0",
                    action = navigate("Loans", "NAV_TARGET_LOANS")
                ),
                WidgetPayload.TabBar.TabItem(
                    id = "check_services", label = "Check services", backgroundColor = "#E6F7F5",
                    action = navigate("CarCheckServices", "NAV_TARGET_CAR_CHECK")
                ),
                WidgetPayload.TabBar.TabItem(
                    id = "insurance", label = "Insurance", backgroundColor = "#FDE8F0",
                    action = navigate("CheckCarInsurance", "NAV_TARGET_CAR_CHECK")
                )
            )
        )
    )

    private val buyCarRail = Widget(
        widgetId = "BuyCarHorizontalRailWidget",
        widgetType = WidgetType.HORIZONTAL_RAIL,
        status = "SUCCESS",
        position = 2,
        payload = WidgetPayload.HorizontalRail(
            header = HeaderRef(
                title = TextRef("Buy car"),
                badge = BadgeRef(text = "Up to ₹80,000 off", backgroundColor = "#E63946", textColor = "#FFFFFF")
            ),
            items = listOf(
                WidgetPayload.HorizontalRail.RailItem(
                    id = "AllUsedCars", title = "All used cars",
                    backgroundImage = ImageRef("https://loremflickr.com/300/300/car?lock=101", "All used cars", "1:1"),
                    backgroundColor = "#EDEBFB",
                    action = navigate("categoryPage", "NAV_TARGET_LISTING", mapOf("filter-collectionId" to "used-cars", "pageTitle" to "All used cars"))
                ),
                WidgetPayload.HorizontalRail.RailItem(
                    id = "BudgetUsedCars", title = "Budget used cars",
                    backgroundImage = ImageRef("https://loremflickr.com/300/300/hatchback,car?lock=102", "Budget used cars", "1:1"),
                    backgroundColor = "#EDEBFB",
                    action = navigate("categoryPage", "NAV_TARGET_LISTING", mapOf("filter-collectionId" to "budget-used-cars", "pageTitle" to "Budget used cars"))
                ),
                WidgetPayload.HorizontalRail.RailItem(
                    id = "PremiumUsedCars", title = "Premium used cars",
                    backgroundImage = ImageRef("https://loremflickr.com/300/300/luxury,car?lock=103", "Premium used cars", "1:1"),
                    backgroundColor = "#EDEBFB",
                    action = navigate("categoryPage", "NAV_TARGET_LISTING", mapOf("filter-collectionId" to "premium-used-cars", "pageTitle" to "Premium used cars"))
                ),
                WidgetPayload.HorizontalRail.RailItem(
                    id = "NewCars", title = "New cars",
                    backgroundImage = ImageRef("https://loremflickr.com/300/300/car,new?lock=104", "New cars", "1:1"),
                    backgroundColor = "#EDEBFB",
                    action = navigate("categoryPage", "NAV_TARGET_LISTING", mapOf("filter-collectionId" to "new-cars", "pageTitle" to "New cars"))
                )
            )
        )
    )

    private val carCheckGrid = Widget(
        widgetId = "CarCheckServicesGridWidget",
        widgetType = WidgetType.CATEGORY_GRID,
        status = "SUCCESS",
        position = 3,
        payload = WidgetPayload.CategoryGrid(
            header = HeaderRef(title = TextRef("Car check services")),
            gridConfig = WidgetPayload.CategoryGrid.GridConfig(rowCount = 2, columnCount = 3),
            items = listOf(
                WidgetPayload.CategoryGrid.GridItem(
                    title = "New car PDI",
                    categoryImage = ImageRef("https://loremflickr.com/80/80/car,checkup?lock=105", "New car PDI"),
                    backgroundColor = "#FBF3E7",
                    action = navigate("NewCarPdi", "NAV_TARGET_CAR_CHECK")
                ),
                WidgetPayload.CategoryGrid.GridItem(
                    title = "Used car check",
                    categoryImage = ImageRef("https://loremflickr.com/80/80/car,mechanic?lock=106", "Used car check"),
                    backgroundColor = "#FBF3E7",
                    action = navigate("UsedCarCheck", "NAV_TARGET_CAR_CHECK")
                ),
                WidgetPayload.CategoryGrid.GridItem(
                    title = "Vehicle history",
                    categoryImage = ImageRef("https://loremflickr.com/80/80/car,garage?lock=107", "Vehicle history"),
                    backgroundColor = "#FBF3E7",
                    action = navigate("VehicleHistory", "NAV_TARGET_CAR_CHECK")
                ),
                WidgetPayload.CategoryGrid.GridItem(
                    title = "Check challan",
                    categoryImage = ImageRef("https://loremflickr.com/80/80/car?lock=108", "Check challan"),
                    backgroundColor = "#FBF3E7",
                    action = navigate("CheckChallan", "NAV_TARGET_CAR_CHECK")
                ),
                WidgetPayload.CategoryGrid.GridItem(
                    title = "Check car insurance",
                    categoryImage = ImageRef("https://loremflickr.com/80/80/car,service?lock=109", "Check car insurance"),
                    backgroundColor = "#FBF3E7",
                    action = navigate("CheckCarInsurance", "NAV_TARGET_CAR_CHECK")
                ),
                WidgetPayload.CategoryGrid.GridItem(
                    title = "Odometer tampering",
                    categoryImage = ImageRef("https://loremflickr.com/80/80/car,dashboard?lock=110", "Odometer tampering"),
                    backgroundColor = "#FBF3E7",
                    action = navigate("OdometerTampering", "NAV_TARGET_CAR_CHECK")
                )
            )
        )
    )

    private val promotionBanner = Widget(
        widgetId = "PromotionBannerWidget",
        widgetType = WidgetType.BANNER_CAROUSEL,
        status = "SUCCESS",
        position = 4,
        payload = WidgetPayload.BannerCarousel(
            items = listOf(
                WidgetPayload.BannerCarousel.BannerItem(
                    id = "SpotifyPromoBanner",
                    image = ImageRef("https://loremflickr.com/800/400/car,keys?lock=111", "Add your car to Orbit - 3 months Spotify Premium free", "2:1"),
                    action = navigate("SellCar", "NAV_TARGET_SELL_CAR"),
                    mediaType = "MEDIA_TYPE_IMAGE"
                ),
                WidgetPayload.BannerCarousel.BannerItem(
                    id = "ReturnGuaranteeBanner",
                    image = ImageRef("https://loremflickr.com/800/400/car,showroom?lock=112", "30 day return guarantee", "2:1"),
                    action = navigate("ReturnPolicy", "NAV_TARGET_RETURN_POLICY"),
                    mediaType = "MEDIA_TYPE_IMAGE"
                )
            ),
            carousel = WidgetPayload.BannerCarousel.CarouselConfig(
                autoScrollEnabled = true, autoScrollIntervalMs = 3000L,
                indicatorEnabled = true, variant = "CAROUSEL_VARIANT_FULL_WIDTH"
            )
        )
    )

    private val usedCarsYoullLove = Widget(
        widgetId = "UsedCarsYoullLoveWidget",
        widgetType = WidgetType.PRODUCT_COLLECTION,
        status = "SUCCESS",
        position = 5,
        payload = WidgetPayload.ProductCollection(
            header = HeaderRef(
                title = TextRef("Used cars you'll love"),
                cta = CtaRef(
                    component = "CTA_COMPONENT_LINK", variant = "CTA_VARIANT_GHOST", label = "View all",
                    action = navigate("categoryPage", "NAV_TARGET_LISTING", mapOf("filter-collectionId" to "used-cars", "pageTitle" to "Used cars you'll love"))
                )
            ),
            filterChips = emptyList(),
            items = listOf(
                WidgetPayload.ProductCollection.ProductItem(
                    productId = "C24-SWIFT-2017-0091", title = "2017 Maruti Swift", subtitle = "VXI",
                    productImage = ImageRef("https://loremflickr.com/400/300/hatchback,car?lock=113", "2017 Maruti Swift"),
                    tag = TextRef("Cars24 Owned stock"),
                    specs = listOf("71,846 km", "Petrol", "Manual", "KA01"),
                    price = WidgetPayload.ProductCollection.ProductItem.Price(
                        sellingPrice = 465000.0, displayPrice = "₹4.65 lakh", emiText = "EMI ₹9,091/m*", footnote = "+other charges"
                    ),
                    badges = listOf(
                        BadgeRef(text = "Zero Worry Max", icon = ImageRef("https://placehold.co/24x24/1E8E3E/FFFFFF?text=%E2%9C%93")),
                        BadgeRef(text = "Lifetime warranty", icon = ImageRef("https://placehold.co/24x24/1E8E3E/FFFFFF?text=%E2%9C%93"))
                    ),
                    wishlistIcon = WidgetPayload.ProductCollection.ProductItem.WishlistIcon(
                        url = "https://placehold.co/24x24/FFFFFF/9B9B9B?text=%E2%99%A5", selected = false
                    ),
                    action = navigate("PDP", "NAV_TARGET_PDP", mapOf("productCode" to "C24-SWIFT-2017-0091")),
                    available = true, status = "PRODUCT_AVAILABILITY_STATUS_AVAILABLE"
                ),
                WidgetPayload.ProductCollection.ProductItem(
                    productId = "C24-PULSE-2015-0044", title = "2015 Renault Pulse", subtitle = "RXL PETROL",
                    productImage = ImageRef("https://loremflickr.com/400/300/sedan,car?lock=114", "2015 Renault Pulse"),
                    tag = TextRef("Cars24 Owned stock"),
                    specs = listOf("44,515 km", "Petrol", "Manual", "KA01"),
                    price = WidgetPayload.ProductCollection.ProductItem.Price(
                        sellingPrice = 232000.0, displayPrice = "₹2.32 lakh", emiText = "EMI ₹6,12x/m*", footnote = "+other charges"
                    ),
                    badges = listOf(
                        BadgeRef(text = "Zero Worry Max", icon = ImageRef("https://placehold.co/24x24/1E8E3E/FFFFFF?text=%E2%9C%93"))
                    ),
                    wishlistIcon = WidgetPayload.ProductCollection.ProductItem.WishlistIcon(
                        url = "https://placehold.co/24x24/FFFFFF/9B9B9B?text=%E2%99%A5", selected = false
                    ),
                    action = navigate("PDP", "NAV_TARGET_PDP", mapOf("productCode" to "C24-PULSE-2015-0044")),
                    available = true, status = "PRODUCT_AVAILABILITY_STATUS_AVAILABLE"
                )
            )
        )
    )

    fun build(): HomeWidgets = HomeWidgets(
        headerWidgets = listOf(searchBar, tabBar),
        contentByTab = mapOf(DEFAULT_TAB_ID to listOf(buyCarRail, carCheckGrid, promotionBanner, usedCarsYoullLove))
    )
}
