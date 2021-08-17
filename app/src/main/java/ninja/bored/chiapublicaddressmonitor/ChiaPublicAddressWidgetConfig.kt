package ninja.bored.chiapublicaddressmonitor

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import com.mikepenz.aboutlibraries.LibsBuilder
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ninja.bored.chiapublicaddressmonitor.helpers.ChiaExplorerApiHelper
import ninja.bored.chiapublicaddressmonitor.helpers.Slh
import ninja.bored.chiapublicaddressmonitor.model.ChiaWidgetRoomsDatabase
import ninja.bored.chiapublicaddressmonitor.model.WidgetSettings

class ChiaPublicAddressWidgetConfig : Activity(), CoroutineScope {

    companion object {
        private const val TAG = "WidgetConfig"
    }

    private var chiaAddressEditText: AutoCompleteTextView? = null
    private var appWidgetID: Int = 0
    private var widgetDB: ChiaWidgetRoomsDatabase? = null
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        setContentView(R.layout.chia_public_address_widget_option_activity)

        appWidgetID = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    override fun onResume() {
        super.onResume()
        chiaAddressEditText = findViewById(R.id.chiaAddressEditText)

        widgetDB = ChiaWidgetRoomsDatabase.getInstance(this)
        val context = this

        findViewById<Button>(R.id.chia_widget_save_button)?.setOnClickListener { saveSettings() }

        launch {
            val widgetCurrentSettings = widgetDB?.getWidgetSettingsDao()?.getByID(
                appWidgetID
            )
            if (widgetCurrentSettings != null) {
                chiaAddressEditText?.setText(widgetCurrentSettings.chiaAddress)
            } else {

                val savedWidgetData = widgetDB?.getWidgetDataDao()?.getAll()
                val savedAddresses = savedWidgetData?.map { it.chiaAddress }

                Log.d(TAG, "savedAddresses: $savedAddresses")

                val adapter = ArrayAdapter(
                    context,
                    android.R.layout.simple_list_item_1, savedAddresses.orEmpty()
                )
                chiaAddressEditText?.threshold = 1
                chiaAddressEditText?.setAdapter(adapter)
                chiaAddressEditText?.setOnFocusChangeListener { v, hasFocus ->
                    if (hasFocus) {
                        v?.let {
                            (v as AutoCompleteTextView).showDropDown()
                        }
                    }
                }
            }
        }

        chiaAddressEditText?.setOnEditorActionListener { _, actionId, _ ->
            if (EditorInfo.IME_ACTION_DONE == actionId) {
                saveSettings()
                false
            } else {
                true
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

    fun saveSettings() {
        chiaAddressEditText?.let {
            val chiaAddress = it.text.toString()
            if (Slh.isChiaAddressValid(chiaAddress.trim())) {
                val widgetSettings = WidgetSettings(appWidgetID, chiaAddress.trim())
                val chiaWidgetSettingsDao = widgetDB?.getWidgetSettingsDao()
                launch {
                    chiaWidgetSettingsDao?.insertUpdate(widgetSettings)
                    updateAppWidget(appWidgetID, widgetSettings)
                    doneWithEverythingAndWentWell(appWidgetID)
                }
            } else {
                chiaAddressEditText?.error = getText(R.string.chia_address_input_error_wrong)
            }
        }
    }

    private suspend fun updateAppWidget(appWidgetID: Int, widgetSettings: WidgetSettings) {
        val context = this

        Log.d("Hans", "getting data")
        val dataDao = widgetDB?.getWidgetDataDao()
        var widgetData = dataDao?.getByAddress(widgetSettings.chiaAddress)
        if (widgetData == null) {
            widgetData = ChiaExplorerApiHelper.receiveWidgetDataFromApi(
                widgetSettings.chiaAddress
            )

            if (widgetData != null) {
                dataDao?.insertUpdate(widgetData)
            }
        }

        widgetData?.let {
            Slh.updateWithWidgetData(
                widgetData,
                context,
                appWidgetID
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
