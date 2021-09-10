package ninja.bored.chiapublicaddressmonitor.model

import com.google.gson.annotations.SerializedName

data class AllTheBlocksApiResponse(
    @SerializedName("address") val address: String,
    @SerializedName("balance") val balance: Long,
    @SerializedName("balanceBefore") val balanceBefore: Long,
    @SerializedName("timestampBalanceBefore") val timestampBalanceBefore: Long,
    @SerializedName("createTimestamp") val createTimestamp: Long
)
