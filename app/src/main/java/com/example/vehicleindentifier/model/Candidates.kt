package com.example.vehicleindentifier.model

import com.google.gson.annotations.SerializedName

data class Candidates(

    @SerializedName("score") var score: Double? = null,
    @SerializedName("plate") var plate: String? = null

)
