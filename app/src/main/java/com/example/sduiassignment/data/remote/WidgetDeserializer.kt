package com.example.sduiassignment.data.remote

import com.example.sduiassignment.data.model.Widget
import com.example.sduiassignment.data.model.WidgetPayload
import com.example.sduiassignment.data.model.WidgetType
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonObject
import java.lang.reflect.Type

/**
 * Widget.json_payload is polymorphic: its shape depends on widget_type, and it's only
 * present when status == SUCCESS. This picks the matching payload class before delegating
 * back to Gson for the actual field mapping.
 */
class WidgetDeserializer : JsonDeserializer<Widget> {

    override fun deserialize(
        json: com.google.gson.JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Widget {
        val obj = json.asJsonObject
        val widgetId = obj.get("widget_id")?.asString.orEmpty()
        val widgetType = obj.get("widget_type")?.asString.orEmpty()
        val status = obj.get("status")?.asString.orEmpty()
        val position = obj.get("position")?.asInt ?: 0
        val payloadJson: JsonObject? = obj.getAsJsonObject("json_payload")

        val payload: WidgetPayload = if (status == "SUCCESS" && payloadJson != null) {
            when (widgetType) {
                WidgetType.SEARCH_BAR ->
                    context.deserialize<WidgetPayload.SearchBar>(payloadJson, WidgetPayload.SearchBar::class.java)
                WidgetType.TAB_BAR ->
                    context.deserialize<WidgetPayload.TabBar>(payloadJson, WidgetPayload.TabBar::class.java)
                WidgetType.HORIZONTAL_RAIL ->
                    context.deserialize<WidgetPayload.HorizontalRail>(payloadJson, WidgetPayload.HorizontalRail::class.java)
                WidgetType.CATEGORY_GRID ->
                    context.deserialize<WidgetPayload.CategoryGrid>(payloadJson, WidgetPayload.CategoryGrid::class.java)
                WidgetType.BANNER_CAROUSEL ->
                    context.deserialize<WidgetPayload.BannerCarousel>(payloadJson, WidgetPayload.BannerCarousel::class.java)
                WidgetType.PRODUCT_COLLECTION ->
                    context.deserialize<WidgetPayload.ProductCollection>(payloadJson, WidgetPayload.ProductCollection::class.java)
                else -> WidgetPayload.Unknown
            }
        } else {
            WidgetPayload.Unknown
        }

        return Widget(
            widgetId = widgetId,
            widgetType = widgetType,
            status = status,
            position = position,
            payload = payload
        )
    }
}
