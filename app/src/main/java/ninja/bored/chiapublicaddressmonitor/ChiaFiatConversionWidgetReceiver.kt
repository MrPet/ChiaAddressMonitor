package ninja.bored.chiapublicaddressmonitor

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import ninja.bored.chiapublicaddressmonitor.helpers.Slh

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

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        Log.d(TAG, "onUpdate")
        context?.let {
            Slh.setupWidgetUpdateWorker(context)
//            all done by worker now

//            val database = ChiaWidgetRoomsDatabase.getInstance(context)
//            appWidgetManager?.let {
//                appWidgetIds?.forEach { appWidgetId ->
//                    GlobalScope.launch {
//                        val widgetFiatConversionSettingsDao =
//                            database.getWidgetFiatConversionSettingsDao()
//                        val widgetFiatConversionSettings =
//                            widgetFiatConversionSettingsDao.getByID(appWidgetId)
//                        widgetFiatConversionSettings?.let {
//                            RemoteViews(
//                                context.packageName,
//                                R.layout.chia_public_address_widget
//                            ).apply {
//                                val allViews = this
//                                Slh.updateFiatWidgetWithSettings(
//                                    widgetFiatConversionSettings,
//                                    allViews,
//                                    context,
//                                    appWidgetId,
//                                    appWidgetManager
//                                )
//                            }
//                        }
//                    }
//                }
//            }
        }
    }
}
