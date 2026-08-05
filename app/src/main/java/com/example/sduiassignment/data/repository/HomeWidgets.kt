package com.example.sduiassignment.data.repository

import com.example.sduiassignment.data.model.Widget

const val DEFAULT_TAB_ID = "all"

/** Widgets split by sublayout: [headerWidgets] render pinned above the scrolling content.
 * [contentByTab] holds one widget list per TabBarWidget tab id (keyed by [Sublayout.tabId]),
 * so tapping a tab can swap in a different content set; [DEFAULT_TAB_ID] is the fallback
 * shown before any tab is tapped and for tabs without their own content set. */
data class HomeWidgets(
    val headerWidgets: List<Widget>,
    val contentByTab: Map<String, List<Widget>>
) {
    fun contentFor(tabId: String?): List<Widget> =
        contentByTab[tabId] ?: contentByTab[DEFAULT_TAB_ID].orEmpty()
}
