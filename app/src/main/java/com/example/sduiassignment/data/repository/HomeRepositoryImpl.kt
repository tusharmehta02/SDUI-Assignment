package com.example.sduiassignment.data.repository

import com.example.sduiassignment.data.model.Sublayout
import com.example.sduiassignment.data.model.SublayoutType
import com.example.sduiassignment.data.model.Widget
import com.example.sduiassignment.data.model.WidgetPayload
import com.example.sduiassignment.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HomeRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : HomeRepository {

    override suspend fun getHomeWidgets(): Result<HomeWidgets> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getHomePage()
            val sublayouts = response.data.sublayouts

            fun renderableWidgets(group: List<Sublayout>): List<Widget> = group
                .asSequence()
                .flatMap { it.widgetgroups.asSequence() }
                .flatMap { it.widgets.asSequence() }
                .filter { it.status == "SUCCESS" && it.payload !is WidgetPayload.Unknown }
                .sortedBy { it.position }
                .toList()

            val headerWidgets = renderableWidgets(sublayouts.filter { it.type == SublayoutType.HEADER })

            val contentByTab = sublayouts
                .filter { it.type == SublayoutType.MAIN_SCREEN }
                .groupBy { it.tabId ?: DEFAULT_TAB_ID }
                .mapValues { (_, group) -> renderableWidgets(group) }

            HomeWidgets(headerWidgets = headerWidgets, contentByTab = contentByTab)
        }
    }
}
