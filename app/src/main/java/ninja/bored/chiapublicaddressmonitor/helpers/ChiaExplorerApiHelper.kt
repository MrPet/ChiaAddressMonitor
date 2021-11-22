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
import ninja.bored.chiapublicaddressmonitor.model.ChiaExplorerAddressResponse
import ninja.bored.chiapublicaddressmonitor.model.WidgetData
import ninja.bored.chiapublicaddressmonitor.model.WidgetDataDao
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

object ChiaExplorerApiHelper {

    const val TAG: String = "chiaExplorerApiHelper"

    suspend fun receiveWidgetDataFromApiAndUpdateToDatabase(
        chiaAddress: String,
        widgetDataDao: WidgetDataDao,
        context: Context,
        showConnectionProblems: Boolean
    ): WidgetData? {
        Log.d(ChiaToFiatConversionApiHelper.TAG, "getting widget data from Api")
        val currentWidgetDataUpdate = receiveWidgetDataFromApi(chiaAddress)
        if (currentWidgetDataUpdate != null) {
            widgetDataDao.insertUpdate(currentWidgetDataUpdate)
        } else if (showConnectionProblems) {
            Toast.makeText(context, R.string.connectionProblems, Toast.LENGTH_LONG)
                .show()
        }
        return currentWidgetDataUpdate
    }

    /**
     * gets Address data from chiaexplorer.com
     */
    suspend fun receiveWidgetDataFromApi(address: String): WidgetData? =
        suspendCancellableCoroutine { continuation ->
            Log.d(TAG, "calling explorer api: $address")
            val request = Request.Builder()
                .url(Constants.BASE_API_URL + address.trim())
                .addHeader(
                    Constants.CHIA_EXPLORER_API_KEY_HEADER_NAME,
                    Constants.CHIA_EXPLORER_API_KEY
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
                    if (response.isSuccessful || response.code == Constants.ADDRESS_NOT_FOUND_HTTP_CODE) {
                        try {
                            val chiaExplorerAddressResult = response.body?.let {
                                Gson().fromJson(
                                    it.charStream(),
                                    ChiaExplorerAddressResponse::class.java
                                )
                            }
                            response.body?.close()
                            chiaExplorerAddressResult?.let {
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
        chiaExplorerAddressResponse: ChiaExplorerAddressResponse,
        date: Date
    ): WidgetData {
        var divider = ForkHelper.getNetBalanceDividerFromAddress(address)
        if (divider == null) {
            divider = Constants.NET_BALANCE_DIVIDER
        }
        Log.d(TAG, "divider$divider")
        val dividedNetBalance = when (chiaExplorerAddressResponse.netBalance) {
            0.0 -> chiaExplorerAddressResponse.netBalance
            else -> chiaExplorerAddressResponse.netBalance.div(divider)
        }
        val dividedGrossBalance = when (chiaExplorerAddressResponse.grossBalance) {
            0.0 -> chiaExplorerAddressResponse.grossBalance
            else -> chiaExplorerAddressResponse.grossBalance.div(divider)
        }
        return WidgetData(
            address,
            dividedNetBalance,
            date,
            dividedGrossBalance
        )
    }
}
