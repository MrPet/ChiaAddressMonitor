package ninja.bored.chiapublicaddressmonitor

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.Log
import android.widget.RemoteViews
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ninja.bored.chiapublicaddressmonitor.helpers.Constants
import ninja.bored.chiapublicaddressmonitor.helpers.NotificationHelper
import ninja.bored.chiapublicaddressmonitor.helpers.Slh
import ninja.bored.chiapublicaddressmonitor.helpers.WidgetHelper
import ninja.bored.chiapublicaddressmonitor.model.AddressSettings
import ninja.bored.chiapublicaddressmonitor.model.ChiaWidgetRoomsDatabase

class ChiaPublicAddressGroupingWidgetReceiver : AppWidgetProvider() {
    companion object {
        private const val TAG = "WidgetGroupReceiver"
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        if (context != null) {
            val database = ChiaWidgetRoomsDatabase.getInstance(context)
            appWidgetIds?.forEach { appWidgetID ->
                GlobalScope.launch {
                    val widgetSettingsDao = database.getWidgetSettingsDao()
                    widgetSettingsDao.getByID(appWidgetID)?.let { widgetSettings ->
                        widgetSettingsDao.delete(widgetSettings)
                    }
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        Log.d(TAG, "onUpdate")
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        context?.let {
            Slh.setupWidgetUpdateWorker(context)
            NotificationHelper.createNotificationChannels(context)
            val database = ChiaWidgetRoomsDatabase.getInstance(context)
            appWidgetManager?.let {
                Log.d(TAG, "appWidgetManager")
                appWidgetIds?.forEach { appWidgetId ->
                    Log.d(TAG, "appWidgetIds")
                    GlobalScope.launch {
                        val widgetAddressGroupSettingsWithAddressesDao =
                            database.getWidgetAddressGroupSettingsWithAddressesDao()

                        val addressGroupingSettings =
                            widgetAddressGroupSettingsWithAddressesDao.getWidgetAddressGroupWithAddresses(
                                appWidgetId
                            )

                        RemoteViews(
                            context.packageName,
                            R.layout.chia_public_address_widget
                        ).apply {
                            Log.d(TAG, "RemoteViews")
                            val allViews = this
                            val widgetDataHelper =
                                WidgetHelper.getSummedWidgetData(database, addressGroupingSettings)
                            if (widgetDataHelper != null) {

                                WidgetHelper.updateWithWidgetData(
                                    widgetDataHelper,
                                    context,
                                    appWidgetId,
                                    AddressSettings(
                                        widgetDataHelper.chiaAddress,
                                        false,
                                        null,
                                        Constants.defaultUpdateTime,
                                        addressGroupingSettings?.widgetAddressGroupSettings?.currency,
                                        false
                                    )
                                )
                            } else {
                                allViews.setTextViewText(
                                    R.id.chia_amount_holder,
                                    context.getText(R.string.loading)
                                )
                                appWidgetManager.updateAppWidget(appWidgetId, allViews)
                            }
                        }
                    }
                }
            }
        }
    }
}
