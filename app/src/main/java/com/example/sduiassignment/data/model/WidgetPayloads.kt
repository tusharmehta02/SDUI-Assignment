package com.example.sduiassignment.data.model

object GridCardStyle {
    const val ICON = "ICON"
    const val IMAGE_CARD = "IMAGE_CARD"
}

/** Discriminated payload for a single widget's json_payload, keyed by widget_type. */
sealed class WidgetPayload {

    data class SearchBar(
        val placeholderText: String?,
        val hintChips: List<String>? = null,
        val variant: String? = null,
        val action: ActionRef? = null
    ) : WidgetPayload()

    data class TabBar(
        val items: List<TabItem>
    ) : WidgetPayload() {
        data class TabItem(
            val id: String?,
            val label: String?,
            val selected: Boolean? = false,
            val backgroundColor: String? = null,
            val action: ActionRef?
        )
    }

    data class HorizontalRail(
        val header: HeaderRef?,
        val items: List<RailItem>
    ) : WidgetPayload() {
        data class RailItem(
            val id: String?,
            val title: String?,
            val backgroundImage: ImageRef?,
            val backgroundColor: String? = null,
            val action: ActionRef?
        )
    }

    data class CategoryGrid(
        val header: HeaderRef?,
        val gridConfig: GridConfig?,
        val items: List<GridItem>
    ) : WidgetPayload() {
        data class GridConfig(
            val rowCount: Int?,
            val columnCount: Int?,
            /** "ICON" (default): small centered icon + title. "IMAGE_CARD": title + subtitle
             * with a larger image, e.g. the "Buy used car" category cards. */
            val cardStyle: String? = null
        )
        data class GridItem(
            val title: String?,
            val subtitle: String? = null,
            val categoryImage: ImageRef?,
            val backgroundColor: String? = null,
            val action: ActionRef?
        )
    }

    data class BannerCarousel(
        val items: List<BannerItem>,
        val carousel: CarouselConfig?
    ) : WidgetPayload() {
        data class BannerItem(
            val id: String?,
            val image: ImageRef?,
            val action: ActionRef? = null,
            val mediaType: String? = null
        )
        data class CarouselConfig(
            val autoScrollEnabled: Boolean? = null,
            val autoScrollIntervalMs: Long? = null,
            val indicatorEnabled: Boolean? = null,
            val variant: String? = null
        )
    }

    data class ProductCollection(
        val header: HeaderRef?,
        val filterChips: List<FilterChip>? = null,
        val items: List<ProductItem>
    ) : WidgetPayload() {
        data class FilterChip(
            val id: String?,
            val label: String?,
            val selected: Boolean? = false,
            val action: ActionRef?
        )

        data class ProductItem(
            val productId: String?,
            val title: String?,
            val subtitle: String? = null,
            val productImage: ImageRef?,
            val tag: TextRef? = null,
            val specs: List<String>? = null,
            val price: Price?,
            val badges: List<BadgeRef>? = null,
            val wishlistIcon: WishlistIcon? = null,
            val action: ActionRef?,
            val available: Boolean? = true,
            val status: String? = null
        ) {
            data class Price(
                val sellingPrice: Double? = null,
                val displayPrice: String? = null,
                val mrp: Double? = null,
                val emiText: String? = null,
                val footnote: String? = null
            )

            data class WishlistIcon(
                val url: String?,
                val selected: Boolean? = false
            )
        }
    }

    /** Unknown / unsupported widget_type, or a widget whose status isn't SUCCESS. */
    object Unknown : WidgetPayload()
}
