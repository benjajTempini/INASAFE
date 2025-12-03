package com.example.inasafe.data.network

import com.google.gson.annotations.SerializedName

// Models for Nearby Stops endpoint
data class NearbyBusStop(
    @SerializedName("stop_id")
    val id: String,
    @SerializedName("stop_name")
    val name: String,
    @SerializedName("stop_lat")
    val lat: Double,
    @SerializedName("stop_lon")
    val lon: Double
)

// Models for Stop Arrivals endpoint
data class ArrivalsResponse(
    @SerializedName("rawData")
    val rawData: RawData?
)

data class RawData(
    @SerializedName("nomett")
    val stopName: String,
    @SerializedName("paradero")
    val stopId: String,
    @SerializedName("servicios")
    val services: ServiceList
)

data class ServiceList(
    @SerializedName("item")
    val items: List<BusService>
)

data class BusService(
    @SerializedName("servicio")
    val serviceName: String,
    @SerializedName("horaprediccionbus1")
    val arrival1: String,
    @SerializedName("horaprediccionbus2")
    val arrival2: String,
    @SerializedName("destino")
    val destination: String,
    @SerializedName("ppubus1")
    val plate1: String?,
    @SerializedName("ppubus2")
    val plate2: String?
)