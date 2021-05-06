package ninja.bored.chiapublicaddressmonitor

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ninja.bored.chiapublicaddressmonitor.helpers.Constants
import ninja.bored.chiapublicaddressmonitor.helpers.NotificationHelper
import ninja.bored.chiapublicaddressmonitor.helpers.Slh
import ninja.bored.chiapublicaddressmonitor.model.ChiaWidgetRoomsDatabase
import ninja.bored.chiapublicaddressmonitor.model.WidgetSettings

class ChiaPublicAddressWidgetReceiver : AppWidgetProvider() {

    private var addressFromReceive: String? = null

    companion object {
        private const val TAG = "WidgetReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive")
        addressFromReceive = intent?.extras?.getString(Constants.ADDRESS_EXTRA)
        val receivedAppWidgetID =
            intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID)
        Log.d(TAG, "receivedAppWidgetID $receivedAppWidgetID")
        val receivedAddress = addressFromReceive
        Log.d(TAG, "receivedAddress $receivedAddress")
        context?.let {
            if (
                receivedAppWidgetID != null &&
                receivedAppWidgetID != INVALID_APPWIDGET_ID &&
                receivedAddress != null
            ) {
                val database = ChiaWidgetRoomsDatabase.getInstance(context)
                val widgetSettings = WidgetSettings(receivedAppWidgetID, receivedAddress)
                val widgetSettingsDao = database.getWidgetSettingsDao()
                GlobalScope.launch {
                    widgetSettingsDao.insertUpdate(widgetSettings)
                    val appWidgetManager = AppWidgetManager.getInstance(context.applicationContext)
                    onUpdate(context, appWidgetManager, intArrayOf(receivedAppWidgetID))
                }
                Toast.makeText(context, R.string.chia_widget_added, Toast.LENGTH_LONG).show()
            } else {
                // doesn't concern me
                if (receivedAppWidgetID != null && receivedAppWidgetID != INVALID_APPWIDGET_ID) {
                    val appWidgetManager = AppWidgetManager.getInstance(context.applicationContext)
                    onUpdate(context, appWidgetManager, intArrayOf(receivedAppWidgetID))
                }
                super.onReceive(context, intent)
            }
        }
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        if (context != null) {
            val database = ChiaWidgetRoomsDatabase.getInstance(context)
            appWidgetIds?.forEach {
                GlobalScope.launch {
                    val widgetSettingsDao = database.getWidgetSettingsDao()
                    val widgetSettingsToDelete = widgetSettingsDao.getByID(it)
                    if (widgetSettingsToDelete != null) {
                        widgetSettingsDao.delete(widgetSettingsToDelete)
                    }
                }
            }
        }
    }

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        context?.let {
            NotificationHelper.createNotificationChannels(context)
            val database = ChiaWidgetRoomsDatabase.getInstance(context)
            appWidgetManager?.let {
                appWidgetIds?.forEach { appWidgetId ->
                    GlobalScope.launch {
                        val widgetSettingsDao = database.getWidgetSettingsDao()
                        val widgetSettings: WidgetSettings?
                        val chiaAddress = addressFromReceive

                        if (chiaAddress != null) {
                            // we come from app and have already the address, but no widget settings
                            widgetSettings = WidgetSettings(appWidgetId, chiaAddress)
                            widgetSettingsDao.insertUpdate(widgetSettings)
                        } else {
                            widgetSettings = widgetSettingsDao.getByID(appWidgetId)
                        }

                        widgetSettings?.let {
                            RemoteViews(
                                context.packageName,
                                R.layout.chia_public_address_widget
                            ).apply {
                                val allViews = this
                                loadAllWidgetDataAndSyncFromApi(
                                    allViews,
                                    widgetSettings,
                                    context,
                                    appWidgetId,
                                    appWidgetManager
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadAllWidgetDataAndSyncFromApi(
        allViews: RemoteViews,
        widgetSettings: WidgetSettings,
        context: Context,
        appWidgetId: Int,
        appWidgetManager: AppWidgetManager
    ) {
        GlobalScope.launch {
            val database = ChiaWidgetRoomsDatabase.getInstance(context)
            val dataDao = database.getWidgetDataDao()
            val oldWidgetData = dataDao.getByAddress(widgetSettings.chiaAddress)
            if (oldWidgetData != null) {
                Log.d(TAG, "got widgetData $oldWidgetData")
                Slh.updateWithWidgetData(
                    oldWidgetData,
                    allViews,
                    context,
                    appWidgetId,
                    appWidgetManager
                )
            } else {
                allViews.setTextViewText(
                    R.id.chiaAmountHolder,
                    context.getText(R.string.loading)
                )
                appWidgetManager.updateAppWidget(appWidgetId, allViews)
            }

            Log.d(TAG, "loading new widget Data")
            val newWidgetData =
                Slh.receiveWidgetDataFromApi(widgetSettings.chiaAddress)

            newWidgetData?.let {
                Log.d(TAG, "loaded new widget Data")
                NotificationHelper.checkIfNecessaryAndSendNotification(
                    oldWidgetData?.chiaAmount,
                    newWidgetData,
                    context
                )
                dataDao.insertUpdate(it)
                Slh.updateWithWidgetData(
                    it,
                    allViews,
                    context,
                    appWidgetId,
                    appWidgetManager
                )
            }
            Log.d(TAG, "loaded but no res :/")
        }
    }
}
