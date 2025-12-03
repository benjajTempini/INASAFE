package com.example.inasafe

import com.example.inasafe.data.network.BusStopApiService
import com.example.inasafe.data.network.NearbyBusStop
import com.example.inasafe.data.network.RetrofitClient

class BusStopRepository {

    private val apiService: BusStopApiService = RetrofitClient.instance

    suspend fun getNearbyStops(latitude: Double, longitude: Double, limit: Int): List<NearbyBusStop> {
        return apiService.getNearbyStops(latitude, longitude, limit)
    }

    suspend fun getStopArrivals(stopId: String) = apiService.getStopArrivals(stopId)
}