package com.example.vehicleindentifier.model

import com.google.gson.annotations.SerializedName

data class Region(

    @SerializedName("code") var code: String? = null,
    @SerializedName("score") var score: Double? = null

)
