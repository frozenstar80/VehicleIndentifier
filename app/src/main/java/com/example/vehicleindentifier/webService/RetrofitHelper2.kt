package com.example.vehicleindentifier.webService

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper2 {

    val baseUrl = "https://script.google.com/"

    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            // We need to add converter factory to
            // convert JSON object to Java object
            .build()
    }
}