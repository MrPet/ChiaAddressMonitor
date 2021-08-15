package ninja.bored.chiapublicaddressmonitor

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RemoteViews
import android.widget.Spinner
import com.mikepenz.aboutlibraries.LibsBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ninja.bored.chiapublicaddressmonitor.helpers.Constants
import ninja.bored.chiapublicaddressmonitor.helpers.Slh
import ninja.bored.chiapublicaddressmonitor.model.ChiaWidgetRoomsDatabase
import ninja.bored.chiapublicaddressmonitor.model.WidgetFiatConversionSettings
import kotlin.coroutines.CoroutineContext

class ChiaFiatConversionWidgetConfig : Activity(), CoroutineScope {

    companion object {
        private const val TAG = "WidgetFiatConfig"
    }

    private var chiaConversionSpinner: Spinner? = null
    private var appWidgetID: Int = 0
    private var widgetDB: ChiaWidgetRoomsDatabase? = null
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        setContentView(R.layout.chia_fiat_conversion_widget_option_activity)

        appWidgetID = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        chiaConversionSpinner = findViewById(R.id.chia_widget_conversion_spinner)

        widgetDB = ChiaWidgetRoomsDatabase.getInstance(this)
        val context = this

        launch {
            val widgetCurrentSettings = widgetDB?.getWidgetFiatConversionSettingsDao()?.getByID(
                appWidgetID
            )
            chiaConversionSpinner?.let {
            // Create an ArrayAdapter using the string array and a default spinner layout
            val chiaConversionKeys = Constants.CHIA_CURRENCY_CONVERSIONS.filter {
                it.value.hardcodedMultiplier == null
            }.keys.toTypedArray()
            ArrayAdapter(context, R.layout.support_simple_spinner_dropdown_item, chiaConversionKeys)
                .also { adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    it.adapter = adapter
                    if (widgetCurrentSettings?.conversionCurrency != null &&
                        chiaConversionKeys.contains(widgetCurrentSettings.conversionCurrency)
                    ) {
                        it.setSelection(
                            chiaConversionKeys.indexOf(widgetCurrentSettings.conversionCurrency)
                        )
                    }
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.license -> {
                LibsBuilder().start(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.default_menu, menu)
        return true
    }

    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    @Suppress("UNUSED_PARAMETER")
    fun saveSettings(view: View?) {
        chiaConversionSpinner?.let {
            val chiaConversionCurrency = it.selectedItem.toString()
            val widgetSettings = WidgetFiatConversionSettings(appWidgetID, chiaConversionCurrency)
            val chiaFiatConversionWidgetSettingsDao = widgetDB?.getWidgetFiatConversionSettingsDao()
            launch {
                chiaFiatConversionWidgetSettingsDao?.insertUpdate(widgetSettings)
                updateAppWidget(appWidgetID, widgetSettings)
                doneWithEverythingAndWentWell(appWidgetID)
            }
        }
    }

    private suspend fun updateAppWidget(appWidgetID: Int, widgetFiatConversionSettings: WidgetFiatConversionSettings) {
        val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(this)
        val context = this
        RemoteViews(this.packageName, R.layout.chia_public_address_widget).also { views ->
            Slh.updateFiatWidgetWithSettings(
                widgetFiatConversionSettings,
                views,
                context,
                appWidgetID,
                appWidgetManager
            )
        }
    }

    private fun doneWithEverythingAndWentWell(appWidgetID: Int) {
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetID)
            this.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        setResult(RESULT_OK, resultValue)
        finish()
    }
}
