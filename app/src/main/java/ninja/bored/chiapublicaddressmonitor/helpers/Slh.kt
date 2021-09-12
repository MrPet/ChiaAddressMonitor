package ninja.bored.chiapublicaddressmonitor.helpers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.icu.text.DecimalFormat
import android.icu.text.DecimalFormatSymbols
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.widget.RemoteViews
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Locale
import java.util.concurrent.TimeUnit
import ninja.bored.chiapublicaddressmonitor.MainActivity
import ninja.bored.chiapublicaddressmonitor.R
import ninja.bored.chiapublicaddressmonitor.WidgetUpdaterWork
import ninja.bored.chiapublicaddressmonitor.helpers.Constants.CHIA_ADDRESS_LENGTH
import ninja.bored.chiapublicaddressmonitor.helpers.Constants.CHIA_ADDRESS_PREFIX
import ninja.bored.chiapublicaddressmonitor.model.ChiaLatestConversion
import ninja.bored.chiapublicaddressmonitor.model.ChiaWidgetRoomsDatabase
import ninja.bored.chiapublicaddressmonitor.model.WidgetData
import ninja.bored.chiapublicaddressmonitor.model.WidgetDataDao
import ninja.bored.chiapublicaddressmonitor.model.WidgetFiatConversionSettings
import ninja.bored.chiapublicaddressmonitor.model.WidgetSettingsAndData

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
     * Update widget data in Widget
     */
    suspend fun updateWithWidgetData(
        currentWidgetData: WidgetData,
        context: Context,
        appWidgetId: Int?
    ) {
        appWidgetId?.let {
            val database = ChiaWidgetRoomsDatabase.getInstance(context)
            val addressSettingsDao = database.getAddressSettingsDao()
            val addressSettings = addressSettingsDao.getByAddress(currentWidgetData.chiaAddress)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val allViews = RemoteViews(context.packageName, R.layout.chia_public_address_widget)

            val currencyCode = when (addressSettings?.conversionCurrency) {
                null -> Constants.CurrencyCode.XCH
                else -> addressSettings.conversionCurrency
            }

            val chiaAmount = when (addressSettings?.useGrossBalance) {
                true -> currentWidgetData.chiaGrossAmount
                else -> currentWidgetData.chiaAmount
            }

            var currencyMultiplier =
                Constants.CHIA_CURRENCY_CONVERSIONS[currencyCode]?.hardcodedMultiplier
            // get currency info
            if (currencyMultiplier == null) {
                val chiaLatestConversion =
                    ChiaToFiatConversionApiHelper.getLatestChiaConversion(currencyCode, database)
                currencyMultiplier = when (chiaLatestConversion?.price) {
                    null -> 1.0
                    else -> chiaLatestConversion.price
                }
            }

            val amountText = formatChiaDecimal(
                (chiaAmount * currencyMultiplier),
                Constants.CHIA_CURRENCY_CONVERSIONS[currencyCode]?.precision
            )

            val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
                .let { intent ->
                    PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                }

            allViews.setOnClickPendingIntent(R.id.widgetRootLayout, pendingIntent)
            allViews.setTextViewText(R.id.chia_amount_holder, amountText)

            allViews.setTextViewText(R.id.chia_amount_title_holder, currencyCode)

            val sdf = SimpleDateFormat(Constants.SHORT_DATE_TIME_FORMAT, Locale.getDefault())

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
     * Update widget data in Widget
     */
    suspend fun updateFiatWidgetWithSettings(
        widgetFiatConversionSettings: WidgetFiatConversionSettings,
        context: Context,
        appWidgetId: Int?,
        appWidgetManager: AppWidgetManager?
    ) {
        appWidgetId?.let {
            val database = ChiaWidgetRoomsDatabase.getInstance(context)
            val chiaLatestConversion = ChiaToFiatConversionApiHelper.getLatestChiaConversion(
                widgetFiatConversionSettings.conversionCurrency, database
            )
            chiaLatestConversion?.let {
                updateFiatWidgetWithData(
                    chiaLatestConversion,
                    context,
                    appWidgetManager,
                    appWidgetId
                )
            }
        }
    }

    fun updateFiatWidgetWithData(
        chiaLatestConversion: ChiaLatestConversion,
        context: Context,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int
    ) {
        RemoteViews(
            context.packageName,
            R.layout.chia_public_address_widget
        ).apply {
            Log.d(TAG, "updating chia fiat widget")
            val allViews = this
            val conversionText =
                Constants.CurrencyCode.XCH + " / " + chiaLatestConversion.priceCurrency
            val amountText = formatChiaDecimal(
                chiaLatestConversion.price,
                Constants.CHIA_CURRENCY_CONVERSIONS[chiaLatestConversion.priceCurrency]?.precision
            )
            val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
                .let { intent ->
                    PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                }

            allViews.setOnClickPendingIntent(R.id.widgetRootLayout, pendingIntent)
            allViews.setTextViewText(
                R.id.chia_amount_holder,
                amountText
            )

            allViews.setTextViewText(
                R.id.chia_amount_title_holder,
                conversionText
            )

            val sdf = SimpleDateFormat(
                Constants.SHORT_DATE_TIME_FORMAT,
                Locale.getDefault()
            )

            val currentDate = sdf.format(chiaLatestConversion.deviceImportDate)
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
    suspend fun refreshAllAddressWidgets(
        data: List<WidgetSettingsAndData>?,
        context: Context,
        database: ChiaWidgetRoomsDatabase,
        showConnectionProblems: Boolean
    ) {
        val widgetDataDao = database.getWidgetDataDao()

        data?.forEach { widgetData ->
            refreshSingleAddressWidget(
                widgetData,
                widgetDataDao,
                context,
                showConnectionProblems
            )
        }
    }

    suspend fun refreshSingleAddressWidget(
        widgetData: WidgetSettingsAndData,
        widgetDataDao: WidgetDataDao,
        context: Context,
        showConnectionProblems: Boolean
    ) {
        var chiaAddress = widgetData.widgetData?.chiaAddress
        if (chiaAddress == null) {
            chiaAddress = widgetData.widgetSettings?.chiaAddress
        }
        chiaAddress?.let {
            // has widget settings
            AllTheBlocksApiHelper.receiveWidgetDataFromApiAndUpdateToDatabase(
                chiaAddress,
                widgetDataDao,
                context,
                showConnectionProblems
            )?.also { newWidgetData ->
                updateWithWidgetData(
                    newWidgetData,
                    context,
                    widgetData.widgetSettings?.widgetID
                )
                widgetData.widgetData?.let { oldWidgetData ->
                    NotificationHelper.checkIfNecessaryAndSendNotification(
                        oldWidgetData.chiaAmount,
                        newWidgetData,
                        context
                    )
                }
            }
        }
    }

    /**
     * gets data from api and try to update widget attached with address
     */
    suspend fun refreshAllFiatConversionWidgets(
        data: List<WidgetFiatConversionSettings>?,
        context: Context
    ) {
        val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)

        data?.forEach { widgetFiatConversionSettings ->
            updateFiatWidgetWithSettings(
                widgetFiatConversionSettings,
                context,
                widgetFiatConversionSettings.widgetID,
                appWidgetManager
            )
        }
    }

    fun formatChiaDecimal(chiaAmount: Double, precision: String?): String? {
        val decimalFormat: DecimalFormat =
            when {
                (precision == Constants.Precision.MOJO) ->
                    DecimalFormat("#,##0")

                (precision == Constants.Precision.TOTAL) ->
                    DecimalFormat("#,##0.00############", DecimalFormatSymbols(Locale.getDefault()))

                (precision == Constants.Precision.FIAT) ->
                    DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.getDefault()))

                else -> when {
                    (chiaAmount > Constants.BIG_AMOUNT_THRESHOLD) -> DecimalFormat("#,##0.##")

                    else -> DecimalFormat("#,##0.00####", DecimalFormatSymbols(Locale.getDefault()))
                }
            }
        return decimalFormat.format(chiaAmount)
    }

    fun setupWidgetUpdateWorker(context: Context) {
//        if (!isUpdateWorkerRunning(context)) {
        val widgetUpdaterWorkRequest: PeriodicWorkRequest =

            PeriodicWorkRequestBuilder<WidgetUpdaterWork>(
                Constants.UPDATE_WORKER_INTERVAL_IN_MINUTES,
                TimeUnit.MINUTES
            )
                .addTag(Constants.UPDATE_WORKER_NAME)
                .build()

        // we keep the old job if we try to add it again
        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork(
                Constants.UPDATE_WORKER_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                widgetUpdaterWorkRequest
            )
//        }
    }

//    private fun isUpdateWorkerRunning(context: Context): Boolean {
//        var isRunning = false
//        try {
//            val status =
//                WorkManager.getInstance(context).getWorkInfosByTag(Constants.UPDATE_WORKER_NAME)
//                    .get()
//            for (workStatus in status) {
//                if (workStatus.state == WorkInfo.State.RUNNING ||
//                    workStatus.state == WorkInfo.State.ENQUEUED
//                ) {
//                    isRunning = true
//                }
//            }
//            return false
//        } catch (e: InterruptedException) {
//            Log.d(TAG, e.toString())
//        } catch (e: ExecutionException) {
//            Log.d(TAG, e.toString())
//        }
//        return isRunning
//    }
}
