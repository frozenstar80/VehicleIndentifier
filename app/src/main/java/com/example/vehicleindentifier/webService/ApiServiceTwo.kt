package com.example.vehicleindentifier.webService

import com.example.vehicleindentifier.model.NumberPlateResponse
import com.example.vehicleindentifier.model.UserData
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiServiceTwo {

    @GET("macros/s/AKfycbz2knM2KCXE7SzNYNfBq6nvHnVfRS_sMD3m8MIrdBzeu_-bGeeA1BqMsRjQBRTfDH1_/exec")
    suspend fun getDetails() : Response<UserData>
}