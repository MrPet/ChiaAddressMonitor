package ninja.bored.chiapublicaddressmonitor.model

import android.util.ArrayMap
import com.google.gson.annotations.SerializedName
import java.util.Date

data class ChiaConversionResponse(
    @SerializedName("state") val state: ChiaConversionResponseState,
    @SerializedName("data") val data: ArrayMap<String, ChiaConversionResponseData>,
)

data class ChiaConversionResponseState(
    @SerializedName("message") val message: String,
    @SerializedName("code") val code: Int,
)

data class ChiaConversionResponseData(
    @SerializedName("baseCurrency") val baseCurrency: String,
    @SerializedName("priceCurrency") val priceCurrency: String,
    @SerializedName("price") val price: Double,
    @SerializedName("updateDateTime") val updateDateTime: Date,
)

