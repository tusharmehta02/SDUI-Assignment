package com.example.sduiassignment.data.repository

interface HomeRepository {
    /** Fetches the SDUI contract and splits it into a pinned header section (search bar,
     * tab bar) and the scrolling content section (rails, grids, banners, products). */
    suspend fun getHomeWidgets(): Result<HomeWidgets>
}
