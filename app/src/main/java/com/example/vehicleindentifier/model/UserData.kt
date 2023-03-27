package com.example.vehicleindentifier.model

import com.google.gson.annotations.SerializedName

data class UserData(
     var data : ArrayList<Data> = arrayListOf()
)

data class Data(
    @SerializedName("plate"  ) var plate  : String? = null,
    @SerializedName("name"   ) var name   : String? = null,
    @SerializedName("gender" ) var gender : String? = null,
    @SerializedName("color"  ) var color  : String? = null,
    @SerializedName("type"  ) var type  : String? = null
)
