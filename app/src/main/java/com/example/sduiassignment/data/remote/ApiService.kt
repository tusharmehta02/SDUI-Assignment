package com.example.sduiassignment.data.remote

import com.example.sduiassignment.data.model.DiscoverPageResponse
import retrofit2.http.GET

interface ApiService {
    @GET("a9f60712115da1ebeb93")
    suspend fun getHomePage(): DiscoverPageResponse
}
