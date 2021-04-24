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
import kotlinx.coroutines.suspendCancellableCoroutine
import ninja.bored.chiapublicaddressmonitor.MainActivity
import ninja.bored.chiapublicaddressmonitor.R
import ninja.bored.chiapublicaddressmonitor.model.ChiaExplorerAddressResponse
import ninja.bored.chiapublicaddressmonitor.model.ChiaWidgetRoomsDatabase
import ninja.bored.chiapublicaddressmonitor.model.WidgetData
import ninja.bored.chiapublicaddressmonitor.model.WidgetSettingsAndData
import okhttp3.*
import java.io.IOException
import java.util.*


class Slh {
    companion object {
        private const val TAG = "Slh"

        /**
         * validate chia address
         */
        fun isChiaAddressValid(chiaAddress: String?): Boolean {
            // should check real specs
            val checkRegex =
                Regex("^" + Constants.CHIA_ADDRESS_PREFIX + "[\\d\\w]{" + (Constants.CHIA_ADDRESS_LENGTH - Constants.CHIA_ADDRESS_PREFIX.length) + "}$")

            if (chiaAddress != null && checkRegex.matches(chiaAddress)) {
                return true
            }
            return false
        }

        /**
         * gets Address data from chiaexplorer.com
         */
        suspend fun receiveWidgetDataFromApi(address: String): WidgetData? {

            val request = Request.Builder()
                .url(Constants.BASE_API_URL + address)
                .addHeader(
                    Constants.CHIA_EXPLORER_API_KEY_HEADER_NAME,
                    Constants.CHIA_EXPLORER_API_KEY
                          )
                .build()

            val client = OkHttpClient.Builder().build()
            return suspendCancellableCoroutine { continuation ->
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e(TAG, e.toString())
                        continuation.resumeWith(Result.success(null))
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            try {
                                val chiaExplorerAddressResult = response.body?.let {
                                    Gson().fromJson(
                                        it.charStream(),
                                        ChiaExplorerAddressResponse::class.java
                                                   )
                                }
                                chiaExplorerAddressResult?.let {
                                    val netBalance = it.netBalance
                                    val updateDate = Date()
                                    continuation.resumeWith(
                                        Result.success(
                                            WidgetData(
                                                address,
                                                (netBalance.div(1000000000000)),
                                                updateDate
                                                      )
                                                      )
                                                           )
                                }
                            } catch (e: Throwable) {
                                // not good something bad happened
                                Log.e(TAG, e.toString())
                                continuation.resumeWith(Result.success(null))
                            }
                        } else {
                            continuation.resumeWith(Result.success(null))
                        }
                    }
                })
            }
        }

        /**
         * Update widget data in Widget
         */
        fun updateWithWidgetData(
            currentWidgetData: WidgetData,
            allViews: RemoteViews,
            context: Context,
            appWidgetId: Int,
            appWidgetManager: AppWidgetManager?
                                ) {
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

        /**
         * gets data from api and try to update widget attached with address
         */
        suspend fun refreshAll(
            data: List<WidgetSettingsAndData>?,
            context: Context,
            database: ChiaWidgetRoomsDatabase
                              ) {
            val widgetDataDao = database.WidgetDataDao()
            val allViews = RemoteViews(context.packageName, R.layout.chia_public_address_widget)
            val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
            // TODO: fix problem if there are more than one widget
            data?.forEach { widgetData ->
                if (widgetData.widgetData != null) {
                    val currentWidgetDataUpdate =
                        receiveWidgetDataFromApi(widgetData.widgetData.chiaAddress)
                    if (currentWidgetDataUpdate != null) {
                        widgetDataDao.insertUpdate(currentWidgetDataUpdate)
                        if (widgetData.widgetSettings != null) {
                            if (appWidgetManager.getAppWidgetInfo(widgetData.widgetSettings.widgetID) != null) {
                                updateWithWidgetData(
                                    currentWidgetDataUpdate,
                                    allViews,
                                    context,
                                    widgetData.widgetSettings.widgetID,
                                    appWidgetManager
                                                    )
                            }
                        }
                    } else {
                        Toast.makeText(context, R.string.connectionProblems, Toast.LENGTH_LONG)
                            .show()
                    }
                }

            }
        }
    }
}