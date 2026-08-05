package com.example.sduiassignment.data.model

import com.google.gson.annotations.SerializedName

data class TextRef(
    val text: String?
)

data class ImageRef(
    val url: String?,
    val altText: String? = null,
    val aspectRatio: String? = null
)

data class BadgeRef(
    val text: String?,
    val backgroundColor: String? = null,
    val textColor: String? = null,
    val icon: ImageRef? = null
)

data class HeaderRef(
    val title: TextRef?,
    val subtitle: TextRef? = null,
    val badge: BadgeRef? = null,
    val cta: CtaRef? = null
)

data class CtaRef(
    val component: String?,
    val variant: String?,
    val label: String?,
    val action: ActionRef?
)

data class DiscoverPagePayload(
    val page: String?,
    val params: Map<String, Any?>? = null,
    val type: String?,
    val navTarget: String?,
    val deeplinkUrl: String? = null
)

data class DiscoverWidgetPayload(
    val widgetId: String?,
    val params: Map<String, Any?>? = null
)

data class OperationPayload(
    val type: String?
)

data class ActionRef(
    val type: String?,
    val viewDomain: String? = null,
    @SerializedName("discoverPage") val discoverPage: DiscoverPagePayload? = null,
    @SerializedName("discoverWidget") val discoverWidget: DiscoverWidgetPayload? = null,
    val operation: OperationPayload? = null
)
