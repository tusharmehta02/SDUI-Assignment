package com.example.sduiassignment.data.remote

import com.example.sduiassignment.data.model.DiscoverPageResponse
import retrofit2.http.GET
import retrofit2.http.Headers

interface ApiService {
    @Headers("Cache-Control: no-cache")
    @GET("a9f60712115da1ebeb93")
    suspend fun getHomePage(): DiscoverPageResponse
}
