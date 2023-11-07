package ninja.bored.chiapublicaddressmonitor

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import ninja.bored.chiapublicaddressmonitor.adapter.ChiaAddressMultiSelectListAdapter
import ninja.bored.chiapublicaddressmonitor.helpers.Constants
import ninja.bored.chiapublicaddressmonitor.helpers.WidgetHelper
import ninja.bored.chiapublicaddressmonitor.model.AddressSettings
import ninja.bored.chiapublicaddressmonitor.model.ChiaWidgetRoomsDatabase
import ninja.bored.chiapublicaddressmonitor.model.WidgetAddressGroupSettingsWithAddresses
import ninja.bored.chiapublicaddressmonitor.model.WidgetAddressGroupingSettings
import ninja.bored.chiapublicaddressmonitor.model.WidgetAddressGroupingSettingsHasAddress
import kotlin.coroutines.CoroutineContext

class ChiaPublicAddressGroupingWidgetConfig : ComponentActivity(), CoroutineScope {

    private var addressListRecycler: RecyclerView? = null
    private var appWidgetID: Int = 0
    private var widgetDB: ChiaWidgetRoomsDatabase? = null
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        setContentView(R.layout.chia_public_address_grouping_widget_option_activity)

        appWidgetID = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    override fun onResume() {
        super.onResume()

        findViewById<Button>(R.id.chia_widget_grouping_save_button)?.setOnClickListener {
            saveSettings()
        }
        val context = this
        // load from db
        val chiaConversionSpinner: Spinner = findViewById(R.id.chia_convertion_spinner)
        chiaConversionSpinner.isEnabled = false
        // Create an ArrayAdapter using the string array and a default spinner layout
        val chiaConversionKeys = Constants.CHIA_CURRENCY_CONVERSIONS.keys.toTypedArray()
        ArrayAdapter(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, chiaConversionKeys)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                chiaConversionSpinner.adapter = adapter
            }

        launch {
            ChiaWidgetRoomsDatabase.getInstance(context).also { chiaWidgetRoomsDb ->
                widgetDB = chiaWidgetRoomsDb

//                val widgetAddressGroupSettingsWithAddressesDao =
//                    chiaWidgetRoomsDb.getWidgetAddressGroupSettingsWithAddressesDao()
//
//                addressGroupingSettings =
//                    widgetAddressGroupSettingsWithAddressesDao.getWidgetAddressGroupWithAddresses(
//                        appWidgetID
//                    )
                addressListRecycler = findViewById(R.id.address_list)

                val layoutManager = LinearLayoutManager(context)
                val dividerItemDecoration = DividerItemDecoration(
                    context, layoutManager.orientation
                )

                addressListRecycler?.addItemDecoration(dividerItemDecoration)
                addressListRecycler?.layoutManager = layoutManager
                setUpRoomsToRecyclerListener()
            }
        }
    }

    private fun setUpRoomsToRecyclerListener() {
        widgetDB?.getWidgetSettingsAndDataDao()?.loadAllLiveData()
            ?.observe(this) {
                addressListRecycler?.adapter =
                    ChiaAddressMultiSelectListAdapter(it, findViewById(R.id.chia_convertion_spinner))
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

    private fun saveSettings() {
        val context = this
        val chiaConversionSpinner: Spinner = findViewById(R.id.chia_convertion_spinner)
        addressListRecycler?.adapter?.let {
            if (it is ChiaAddressMultiSelectListAdapter) {
                launch {
                    val addressList = ArrayList<WidgetAddressGroupingSettingsHasAddress>()
                    it.selectedAddressesList.forEach { address ->
                        addressList.add(
                            WidgetAddressGroupingSettingsHasAddress(
                                appWidgetID,
                                address
                            )
                        )
                    }
                    widgetDB?.let { db ->
                        val addressGroupSettingsWithAddresses =
                            WidgetAddressGroupSettingsWithAddresses(
                                WidgetAddressGroupingSettings(
                                    appWidgetID,
                                    chiaConversionSpinner.selectedItem.toString()
                                ),
                                addressList
                            )
                        addressGroupSettingsWithAddresses.insertUpdate(db)
                        WidgetHelper.getSummedWidgetData(db, addressGroupSettingsWithAddresses)
                            ?.let { widgetDataHelper ->
                                WidgetHelper.updateWithWidgetData(
                                    widgetDataHelper,
                                    context,
                                    addressGroupSettingsWithAddresses.widgetAddressGroupSettings.widgetID,
                                    AddressSettings(
                                        widgetDataHelper.chiaAddress,
                                        false,
                                        null,
                                        Constants.defaultUpdateTime,
                                        addressGroupSettingsWithAddresses.widgetAddressGroupSettings.currency,
                                        false
                                    )
                                )
                            }
                        doneWithEverythingAndWentWell(appWidgetID)
                    }
                }
            }
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
