package ninja.bored.chiapublicaddressmonitor.helpers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import ninja.bored.chiapublicaddressmonitor.MainActivity
import ninja.bored.chiapublicaddressmonitor.R
import ninja.bored.chiapublicaddressmonitor.helpers.Constants.CHIA_ADDRESS_LENGTH
import ninja.bored.chiapublicaddressmonitor.helpers.Constants.CHIA_ADDRESS_PREFIX
import ninja.bored.chiapublicaddressmonitor.model.ChiaConversionResponse
import ninja.bored.chiapublicaddressmonitor.model.ChiaExplorerAddressResponse
import ninja.bored.chiapublicaddressmonitor.model.ChiaLatestConversion
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

    object Precision {
        const val TOTAL = "TOTAL"
        const val MOJO = "MOJO"
        const val NORMAL = "Normal"
    }

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
            0.0 -> 0.0
            else -> chiaExplorerAddressResponse.netBalance.div(Constants.NET_BALANCE_DIVIDER)
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
    suspend fun updateWithWidgetData(
        currentWidgetData: WidgetData,
        allViews: RemoteViews,
        context: Context,
        appWidgetId: Int?,
        appWidgetManager: AppWidgetManager?
    ) {
        appWidgetId?.let {
            val database = ChiaWidgetRoomsDatabase.getInstance(context)
            val addressSettingsDao = database.getAddressSettingsDao()
            val addressSettings = addressSettingsDao.getByAddress(currentWidgetData.chiaAddress)


            val currencyCode = when (addressSettings?.conversionCurrency) {
                null -> {
                    Constants.CHIA_CURRENCY_CONVERSIONS[0]
                }
                else -> {
                    addressSettings.conversionCurrency
                }
            }

            // get currency info
            val chiaLatestConversion = getLatestChiaConversion(currencyCode, database)
            val currencyMultiplier: Double = when(chiaLatestConversion?.price){
                null -> {
                    1.0
                }
                else -> {
                    chiaLatestConversion.price
                }
            }

            val amountText = context.resources?.getString(
                R.string.chia_amount_placeholder,
                formatChiaDecimal( (currentWidgetData.chiaAmount * currencyMultiplier), addressSettings?.precision)
            )

            val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
                .let { intent ->
                    PendingIntent.getActivity(context, 0, intent, 0)
                }
            allViews.setOnClickPendingIntent(R.id.widgetRootLayout, pendingIntent)
            allViews.setTextViewText(
                R.id.chia_amount_holder,
                amountText
            )

            if (addressSettings?.precision == Precision.MOJO) {
                allViews.setTextViewText(
                    R.id.chia_amount_title_holder,
                    context.getText(R.string.mojo_amount)
                )
            } else {
                allViews.setTextViewText(
                    R.id.chia_amount_title_holder,
                    currencyCode
                )
            }
            val sdf = SimpleDateFormat(
                Constants.SHORT_DATE_TIME_FORMAT,
                Locale.getDefault()
            )

            val currentDate = sdf.format(currentWidgetData.updateDate)
            allViews.setTextViewText(
                R.id.chia_last_update_holder,
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

    fun formatChiaDecimal(chiaAmount: Double, precision: String?): String? {
        val decimalFormat: DecimalFormat
        var amount = chiaAmount
        if (precision == Precision.MOJO) {
            decimalFormat = DecimalFormat("#,##0")
            amount = chiaAmount * Constants.NET_BALANCE_DIVIDER
        } else if (precision == Precision.TOTAL) {
            decimalFormat =
                DecimalFormat("#,##0.00############", DecimalFormatSymbols(Locale.getDefault()))
            //         if (chiaAmount > 10000) {
            //             decimalFormat = DecimalFormat("#,##0.##")
            //         }
            return decimalFormat.format(chiaAmount)
        } else { // "normal is default
            if (chiaAmount > Constants.BIG_AMOUNT_THRESHOLD) {
                decimalFormat = DecimalFormat("#,##0.##")
            } else {
                decimalFormat =
                    DecimalFormat("#,##0.00####", DecimalFormatSymbols(Locale.getDefault()))
            }
        }
        return decimalFormat.format(amount)
    }

    /**
     * checks in db if price is over threshold if so we get newer prices from api, save them
     * and then return newest price
     */
    suspend fun getLatestChiaConversion(
        conversionCurrency: String,
        database: ChiaWidgetRoomsDatabase
    ): ChiaLatestConversion? {
        val latestCurrencyFromDb = database.getChiaLatestConversionDaoDao().getLatestForCurrency(conversionCurrency)

        val calendar: Calendar = Calendar.getInstance()
        calendar.add(Calendar.MINUTE, Constants.TIME_THRESHOLD_FOR_FIAT_CONVERSION)
        if( latestCurrencyFromDb != null && latestCurrencyFromDb.deviceImportDate.after( Date(calendar.timeInMillis) ) ) // still in threshold we return db
        {
            return latestCurrencyFromDb
        }
        else
        {
            // we need to get from api
            val currencyFromApi = receiveChiaConversionFromApi()
            currencyFromApi?.let{
                var returnChiaConversion: ChiaLatestConversion?  = null
                currencyFromApi.data.forEach{
                    val chiaConverssionResponseData = it.value
                    val newChiaConversion = ChiaLatestConversion(chiaConverssionResponseData.priceCurrency, chiaConverssionResponseData.price, chiaConverssionResponseData.updateDateTime, Date())
                    database.getChiaLatestConversionDaoDao().insertUpdate(newChiaConversion)
                    if( chiaConverssionResponseData.priceCurrency.endsWith(conversionCurrency) ) {
                        returnChiaConversion = newChiaConversion
                    }
                }
                return returnChiaConversion
            }
        }
        return null
    }

    /**
     * gets Conversion Data from conversion cache
     */
    suspend fun receiveChiaConversionFromApi(): ChiaConversionResponse? =
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
                            continuation.resumeWith(Result.success(null))
                        }
                    } else {
                        Log.e(TAG, "Response not successful")
                        continuation.resumeWith(Result.success(null))
                    }
                }
            })
        }
}
