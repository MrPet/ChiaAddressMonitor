package ninja.bored.chiapublicaddressmonitor.model

import com.google.gson.annotations.SerializedName

data class ChiaExplorerAddressResponse(
    @SerializedName("grossBalance") val grossBalance: Double,
    @SerializedName("netBalance") val netBalance: Double,
)
