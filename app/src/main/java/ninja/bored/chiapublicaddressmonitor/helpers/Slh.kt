package ninja.bored.chiapublicaddressmonitor.helpers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonParseException
import kotlinx.coroutines.suspendCancellableCoroutine
import ninja.bored.chiapublicaddressmonitor.MainActivity
import ninja.bored.chiapublicaddressmonitor.R
import ninja.bored.chiapublicaddressmonitor.helpers.Constants.CHIA_ADDRESS_LENGTH
import ninja.bored.chiapublicaddressmonitor.helpers.Constants.CHIA_ADDRESS_PREFIX
import ninja.bored.chiapublicaddressmonitor.model.ChiaExplorerAddressResponse
import ninja.bored.chiapublicaddressmonitor.model.ChiaWidgetRoomsDatabase
import ninja.bored.chiapublicaddressmonitor.model.WidgetData
import ninja.bored.chiapublicaddressmonitor.model.WidgetSettingsAndData
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.Date
import java.util.Locale

object Slh {
    private const val TAG = "Slh"

    /**
     * validate chia address
     */
    fun isChiaAddressValid(chiaAddress: String?): Boolean {
        // should check real specs
        val checkRegex =
            Regex("^$CHIA_ADDRESS_PREFIX[\\d\\w]{${CHIA_ADDRESS_LENGTH - CHIA_ADDRESS_PREFIX.length}}$")
        if (checkRegex.matches(chiaAddress.toString())) {
            return true
        }
        return false
    }

    /**
     * gets Address data from chiaexplorer.com
     */
    suspend fun receiveWidgetDataFromApi(address: String): WidgetData? =

        suspendCancellableCoroutine { continuation ->
            val request = Request.Builder()
                .url(Constants.BASE_API_URL + address)
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
                            continuation.resumeWith(Result.success(null))
                        }
                    } else {
                        Log.e(TAG, "Response not successful")
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
        val dividedNetBalance = when (chiaExplorerAddressResponse.netBalance) {
            0.0 -> {
                0.0
            }
            else -> {
                chiaExplorerAddressResponse.netBalance.div(Constants.NET_BALANCE_DIVIDER)
            }
        }
        return WidgetData(
            address,
            dividedNetBalance,
            date
        )
    }

    /**
     * Update widget data in Widget
     */
    fun updateWithWidgetData(
        currentWidgetData: WidgetData,
        allViews: RemoteViews,
        context: Context,
        appWidgetId: Int?,
        appWidgetManager: AppWidgetManager?
    ) {
        appWidgetId?.let {
            val amountText = context.resources?.getString(
                R.string.chia_amount_placeholder,
                (currentWidgetData.chiaAmount)
            )

            val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
                .let { intent ->
                    PendingIntent.getActivity(context, 0, intent, 0)
                }
            allViews.setOnClickPendingIntent(R.id.widgetRootLayout, pendingIntent)
            allViews.setTextViewText(
                R.id.chiaAmountHolder,
                amountText
            )

            val sdf = SimpleDateFormat(
                Constants.SHORT_DATE_TIME_FORMAT,
                Locale.getDefault()
            )

            val currentDate = sdf.format(currentWidgetData.updateDate)
            allViews.setTextViewText(
                R.id.chiaLastUpdateHolder,
                context.resources?.getString(
                    R.string.last_refresh_placeholder,
                    currentDate
                )
            )
            appWidgetManager?.updateAppWidget(appWidgetId, allViews)
        }
    }

    /**
     * gets data from api and try to update widget attached with address
     */
    suspend fun refreshAll(
        data: List<WidgetSettingsAndData>?,
        context: Context,
        database: ChiaWidgetRoomsDatabase
    ) {
        val widgetDataDao = database.getWidgetDataDao()
        val allViews = RemoteViews(context.packageName, R.layout.chia_public_address_widget)
        val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)

        data?.forEach { widgetData ->
            widgetData.widgetData?.let {
                val currentWidgetDataUpdate =
                    receiveWidgetDataFromApi(widgetData.widgetData.chiaAddress)
                if (currentWidgetDataUpdate != null) {
                    widgetDataDao.insertUpdate(currentWidgetDataUpdate)
                    updateWithWidgetData(
                        currentWidgetDataUpdate,
                        allViews,
                        context,
                        widgetData.widgetSettings?.widgetID,
                        appWidgetManager
                    )
                } else {
                    Toast.makeText(context, R.string.connectionProblems, Toast.LENGTH_LONG)
                        .show()
                }
            }
        }
    }
}
