package com.example.inasafe.data.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BusStopApiService {

    @GET("paraderos/nearby/search")
    suspend fun getNearbyStops(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("limit") limit: Int
    ): List<NearbyBusStop>

    @GET("paraderos/{stopId}")
    suspend fun getStopArrivals(
        @Path("stopId") stopId: String
    ): ArrivalsResponse
}