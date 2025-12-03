package com.example.inasafe.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "https://paquetomilamicro-api-495f1595d7d5.herokuapp.com/"

    val instance: BusStopApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(BusStopApiService::class.java)
    }
}