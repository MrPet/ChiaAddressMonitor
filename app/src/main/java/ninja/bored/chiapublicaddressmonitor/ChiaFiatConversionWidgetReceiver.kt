package ninja.bored.chiapublicaddressmonitor

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ninja.bored.chiapublicaddressmonitor.helpers.Slh
import ninja.bored.chiapublicaddressmonitor.helpers.WidgetHelper
import ninja.bored.chiapublicaddressmonitor.model.ChiaWidgetRoomsDatabase

class ChiaFiatConversionWidgetReceiver : AppWidgetProvider() {

    companion object {
        private const val TAG = "WidgetConvRecvr"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val receivedAppWidgetID =
            intent?.extras?.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        if (receivedAppWidgetID != null && receivedAppWidgetID != AppWidgetManager.INVALID_APPWIDGET_ID) {
            val appWidgetManager = AppWidgetManager.getInstance(context?.applicationContext)
            onUpdate(context, appWidgetManager, intArrayOf(receivedAppWidgetID))
        }
        super.onReceive(context, intent)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        if (context != null) {
            val database = ChiaWidgetRoomsDatabase.getInstance(context)
            appWidgetIds?.forEach { appWidgetID ->
                GlobalScope.launch {
                    val widgetFiatConversionSettingsDao =
                        database.getWidgetFiatConversionSettingsDao()
                    widgetFiatConversionSettingsDao.getByID(appWidgetID)
                        ?.let { widgetFiatConversionSettings ->
                            widgetFiatConversionSettingsDao.delete(widgetFiatConversionSettings)
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
        context?.let {
            Slh.setupWidgetUpdateWorker(context)

            val database = ChiaWidgetRoomsDatabase.getInstance(context)
            appWidgetManager?.let {
                appWidgetIds?.forEach { appWidgetId ->
                    GlobalScope.launch {
                        val widgetFiatConversionSettingsDao =
                            database.getWidgetFiatConversionSettingsDao()
                        val widgetFiatConversionSettings =
                            widgetFiatConversionSettingsDao.getByID(appWidgetId)
                        widgetFiatConversionSettings?.let {

                            val chiaLatestConversionDao = database.getChiaLatestConversionDao()
                            chiaLatestConversionDao.getLatestForCurrency(
                                widgetFiatConversionSettings.conversionCurrency
                            )?.let { chiaLatestConversionFromDb ->
                                WidgetHelper.updateFiatWidgetWithData(
                                    chiaLatestConversionFromDb,
                                    context,
                                    appWidgetManager,
                                    appWidgetId
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
