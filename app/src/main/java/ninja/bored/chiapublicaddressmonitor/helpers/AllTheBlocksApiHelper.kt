package ninja.bored.chiapublicaddressmonitor.helpers

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonParseException
import java.io.IOException
import java.util.Date
import kotlinx.coroutines.suspendCancellableCoroutine
import ninja.bored.chiapublicaddressmonitor.R
import ninja.bored.chiapublicaddressmonitor.model.AllTheBlocksApiResponse
import ninja.bored.chiapublicaddressmonitor.model.WidgetData
import ninja.bored.chiapublicaddressmonitor.model.WidgetDataDao
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

object AllTheBlocksApiHelper {

    const val TAG: String = "allTheBlocksApiHelper"

    suspend fun receiveWidgetDataFromApiAndUpdateToDatabase(
        chiaAddress: String,
        widgetDataDao: WidgetDataDao,
        context: Context,
        showConnectionProblems: Boolean
    ): WidgetData? {
        Log.d(ChiaToFiatConversionApiHelper.TAG, "getting widget data from Api")
        val currentWidgetDataUpdate =
            receiveWidgetDataFromApi(chiaAddress)
        if (currentWidgetDataUpdate != null) {
            widgetDataDao.insertUpdate(currentWidgetDataUpdate)
        } else if (showConnectionProblems) {
            Toast.makeText(context, R.string.connectionProblems, Toast.LENGTH_LONG)
                .show()
        }
        return currentWidgetDataUpdate
    }

    fun buildUrlFromAddress(address: String): String {

        return Constants.BASE_ALL_THE_BLOCKS_API_URL +
                ForkHelper.getCurrencyIdentifierFromAddress(address) +
                Constants.BASE_ALL_THE_BLOCKS_API_ADDRESS_PATH +
                address
    }

    /**
     * gets Address data from api.alltheblocks.net
     */
    suspend fun receiveWidgetDataFromApi(address: String): WidgetData? =
        suspendCancellableCoroutine { continuation ->
            val allTheBlocksUrl = buildUrlFromAddress(address)
            Log.d(TAG, "calling api.alltheblocks.net: $allTheBlocksUrl")
            val request = Request.Builder()
                .url(allTheBlocksUrl)
                .build()

            val client = OkHttpClient.Builder().build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e(TAG, e.toString())
                    continuation.resumeWith(Result.success(null))
                }

                override fun onResponse(call: Call, response: Response) {
                    // never seen addresses get a 404 but if you start farming you still would like to add it ...
                    if (response.isSuccessful) {
                        try {
                            val allTheBlocksAddressResult = response.body?.let {
                                Gson().fromJson(
                                    it.string(),
                                    AllTheBlocksApiResponse::class.java
                                )
                            }
                            response.body?.close()
                            allTheBlocksAddressResult?.let {
                                Log.d(TAG, it.toString())
                                continuation.resumeWith(
                                    Result.success(
                                        parseApiResponseToWidgetData(
                                            address,
                                            it,
                                            Date()
                                        )
                                    )
                                )
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

    fun parseApiResponseToWidgetData(
        address: String,
        allTheBlocksApiResponse: AllTheBlocksApiResponse,
        date: Date
    ): WidgetData {
        var divider = ForkHelper.getNetBalanceDividerFromAddress(address)
        if (divider == null) {
            divider = Constants.NET_BALANCE_DIVIDER
        }
        val dividedNetBalance = when (allTheBlocksApiResponse.balance) {
            0L -> allTheBlocksApiResponse.balance.toDouble()
            else -> allTheBlocksApiResponse.balance.div(divider)
        }
        return WidgetData(
            address,
            dividedNetBalance,
            date,
            dividedNetBalance
        )
    }
}
