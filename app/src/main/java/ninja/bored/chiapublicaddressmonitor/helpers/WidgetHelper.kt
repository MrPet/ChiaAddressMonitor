package ninja.bored.chiapublicaddressmonitor.helpers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.widget.RemoteViews
import java.util.Date
import java.util.Locale
import ninja.bored.chiapublicaddressmonitor.MainActivity
import ninja.bored.chiapublicaddressmonitor.R
import ninja.bored.chiapublicaddressmonitor.model.AddressSettings
import ninja.bored.chiapublicaddressmonitor.model.ChiaLatestConversion
import ninja.bored.chiapublicaddressmonitor.model.ChiaWidgetRoomsDatabase
import ninja.bored.chiapublicaddressmonitor.model.WidgetAddressGroupSettingsWithAddresses
import ninja.bored.chiapublicaddressmonitor.model.WidgetData
import ninja.bored.chiapublicaddressmonitor.model.WidgetDataDao
import ninja.bored.chiapublicaddressmonitor.model.WidgetFiatConversionSettings
import ninja.bored.chiapublicaddressmonitor.model.WidgetSettingsAndData

object WidgetHelper {
    private const val TAG = "WidgetHelper"

    /**
     * Update widget data in Widget
     */
    suspend fun updateWithWidgetData(
        currentWidgetData: WidgetData,
        context: Context,
        appWidgetId: Int?,
        addressSettingsOverride: AddressSettings?
    ) {
        appWidgetId?.let {
            val database = ChiaWidgetRoomsDatabase.getInstance(context)
            val addressSettingsDao = database.getAddressSettingsDao()
            val addressSettings = when (addressSettingsOverride) {
                null -> addressSettingsDao.getByAddress(currentWidgetData.chiaAddress)
                else -> addressSettingsOverride
            }
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

            val amountText = Slh.formatChiaDecimal(
                (chiaAmount * currencyMultiplier),
                Constants.CHIA_CURRENCY_CONVERSIONS[currencyCode]?.precision
            )

            val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
                .let { intent ->
                    PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                }

            allViews.setOnClickPendingIntent(R.id.widgetRootLayout, pendingIntent)
            allViews.setTextViewText(R.id.chia_amount_holder, amountText)

            allViews.setTextViewText(
                R.id.chia_amount_title_holder, when (currencyCode) {
                    Constants.CurrencyCode.XCH -> Slh.getCurrencySymbolFromAddress(currentWidgetData.chiaAddress)
                    else -> currencyCode
                }
            )

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
            val amountText = Slh.formatChiaDecimal(
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
                    widgetData.widgetSettings?.widgetID,
                    null
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

    suspend fun refreshAllGroupedWidgets(
        widgetGroupedAddressSettings: List<WidgetAddressGroupSettingsWithAddresses>?,
        context: Context,
        database: ChiaWidgetRoomsDatabase
    ) {
        widgetGroupedAddressSettings?.forEach { groupedAddressSetting ->
            getSummedWidgetData(database, groupedAddressSetting)?.let {
                updateWithWidgetData(
                    it,
                    context,
                    groupedAddressSetting.widgetAddressGroupSettings.widgetID,
                    AddressSettings(
                        it.chiaAddress,
                        false,
                        null,
                        Constants.defaultUpdateTime,
                        groupedAddressSetting.widgetAddressGroupSettings.currency,
                        false
                    )
                )
            }
        }
    }

    suspend fun getSummedWidgetData(
        database: ChiaWidgetRoomsDatabase,
        addressGroupingSettings: WidgetAddressGroupSettingsWithAddresses?
    ): WidgetData? {
        val widgetDataDao = database.getWidgetDataDao()
        // get all address data

        var summedChiaAmount = 0.0
        var summedChiaGrossAmount = 0.0
        var allAddressesHadData = addressGroupingSettings != null
        var chiaAddress = "multi1"
        addressGroupingSettings?.widgetAddresses?.forEach addressForEach@{
            // load this stuff
            chiaAddress = it.chiaAddress
            val currentWidgetData = widgetDataDao.getByAddress(it.chiaAddress)
            if (currentWidgetData != null) {
                summedChiaAmount += currentWidgetData.chiaAmount
                summedChiaGrossAmount += currentWidgetData.chiaGrossAmount
            } else {
                allAddressesHadData = false
                return@addressForEach
            }
        }
        return when (allAddressesHadData) {
            true -> WidgetData(
                chiaAddress,
                summedChiaAmount,
                Date(),
                summedChiaGrossAmount
            )
            false -> null
        }
    }
}
