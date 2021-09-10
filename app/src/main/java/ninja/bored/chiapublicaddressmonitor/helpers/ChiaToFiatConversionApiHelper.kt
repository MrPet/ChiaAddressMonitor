package ninja.bored.chiapublicaddressmonitor.helpers

import android.icu.util.Calendar
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParseException
import java.io.IOException
import java.util.Date
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import ninja.bored.chiapublicaddressmonitor.model.ChiaConversionResponse
import ninja.bored.chiapublicaddressmonitor.model.ChiaLatestConversion
import ninja.bored.chiapublicaddressmonitor.model.ChiaWidgetRoomsDatabase
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

object ChiaToFiatConversionApiHelper {

    const val TAG: String = "ChiaToFiatConversionApiHelper"

    /**
     * checks in db if price is over threshold if so we get newer prices from api, save them
     * and then return newest price
     */
    suspend fun getLatestChiaConversion(
        conversionCurrency: String,
        database: ChiaWidgetRoomsDatabase
    ): ChiaLatestConversion? {
        var returnChiaConversion: ChiaLatestConversion? =
            database.getChiaLatestConversionDao().getLatestForCurrency(conversionCurrency)

        val calendar: Calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, (-1 * Constants.TIME_THRESHOLD_FOR_FIAT_CONVERSION))
        if (returnChiaConversion == null ||
            returnChiaConversion.deviceImportDate.before(Date(calendar.timeInMillis))
        ) // still in threshold we return db
        {
            Log.d(TAG, "getting conversion from Api")
            // we need to get from api
            receiveChiaConversionFromApi()?.data?.forEach {
                val chiaConversionResponseData = it.value
                val newChiaConversion = ChiaLatestConversion(chiaConversionResponseData)
                database.getChiaLatestConversionDao().insertUpdate(newChiaConversion)
                if (chiaConversionResponseData.priceCurrency == conversionCurrency) {
                    returnChiaConversion = newChiaConversion
                }
            }
        }
        return returnChiaConversion
    }

    /**
     * gets Conversion Data from conversion cache
     */
    private suspend fun receiveChiaConversionFromApi(): ChiaConversionResponse? =
        suspendCancellableCoroutine { continuation: CancellableContinuation<ChiaConversionResponse?> ->
            val request = Request.Builder()
                .url(Constants.CHIA_CONVERSIONS_BASE_API_URL)
                .addHeader(
                    Constants.CHIA_CONVERSIONS_API_KEY_HEADER_NAME,
                    Constants.CHIA_CONVERSIONS_API_KEY
                )
                .build()

            val client = OkHttpClient.Builder().build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, e.toString())
                    continuation.resumeWith(Result.success(null))
                }

                override fun onResponse(call: Call, response: Response) {
                    // newer seen addresses get a 404 but if you start farming you still would like to add it ...
                    if (response.isSuccessful) {
                        try {
                            val chiaExplorerAddressResult = response.body?.let {
                                Gson().fromJson(
                                    it.charStream(),
                                    ChiaConversionResponse::class.java
                                )
                            }
                            response.body?.close()
                            chiaExplorerAddressResult?.let {
                                if (it.state.code == Constants.STATE_OK_CODE) {
                                    continuation.resumeWith(Result.success(it))
                                } else {
                                    continuation.resumeWith(Result.success(null))
                                }
                            }
                        } catch (e: JsonParseException) {
                            // not good something bad happened
                            Log.e(TAG, "ERROR in api response: $e")
                            response.body?.close()
                            continuation.resumeWith(Result.success(null))
                        }
                    } else {
                        Log.e(TAG, "Response not successful")
                        response.body?.close()
                        continuation.resumeWith(Result.success(null))
                    }
                }
            })
        }
}
