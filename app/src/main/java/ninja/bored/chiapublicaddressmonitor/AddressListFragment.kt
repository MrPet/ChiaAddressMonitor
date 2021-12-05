package ninja.bored.chiapublicaddressmonitor

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.WhichButton
import com.afollestad.materialdialogs.actions.setActionButtonEnabled
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import ninja.bored.chiapublicaddressmonitor.adapter.ChiaAddressListAdapter
import ninja.bored.chiapublicaddressmonitor.helpers.AllTheBlocksApiHelper
import ninja.bored.chiapublicaddressmonitor.helpers.ForkHelper
import ninja.bored.chiapublicaddressmonitor.helpers.WidgetHelper
import ninja.bored.chiapublicaddressmonitor.model.ChiaWidgetRoomsDatabase

// https://developer.android.com/guide/topics/appwidgets/#Pinning
/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class AddressListFragment : Fragment() {

    private val database by lazy { this.context?.let { ChiaWidgetRoomsDatabase.getInstance(it) } }
    private var addressListRecycler: RecyclerView? = null
    private var loadingSpinner: View? = null

    companion object {
        private const val TAG: String = "AddressListFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_address_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.context?.let { context ->
            val addAddressButton = view.findViewById<FloatingActionButton>(R.id.addAddressButton)
            val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
            swipeRefreshLayout.setOnRefreshListener {
                database?.let { db ->
                    this.lifecycleScope.launch {
                        WidgetHelper.refreshAllFiatConversionWidgets(
                            db.getWidgetFiatConversionSettingsDao().loadAll(),
                            context
                        )
                        WidgetHelper.refreshAllAddressWidgets(
                            (addressListRecycler?.adapter as ChiaAddressListAdapter).getData(),
                            context,
                            db,
                            true
                        )
                        WidgetHelper.refreshAllGroupedWidgets(
                            db.getWidgetAddressGroupSettingsWithAddressesDao().loadAll(),
                            context,
                            db
                        )
                        swipeRefreshLayout.isRefreshing = false
                    }
                }
            }

            addAddressButton.setOnClickListener { addNewAddress() }
            loadingSpinner = view.findViewById(R.id.loading_spinner)
            addressListRecycler = view.findViewById(R.id.address_list)

            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    // this method is called
                    // when the item is moved.
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val listAdapter = addressListRecycler?.adapter as ChiaAddressListAdapter

                    val position = viewHolder.absoluteAdapterPosition

                    val toDeleteWidgetAndSettingsAndData = listAdapter.getDataFromPosition(position)
                    val toDeleteWidgetData = toDeleteWidgetAndSettingsAndData.widgetData
                    val toDeleteWidgetSettings = toDeleteWidgetAndSettingsAndData.widgetSettings
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    if (toDeleteWidgetSettings == null || appWidgetManager.getAppWidgetIds(
                            ComponentName(context, ChiaPublicAddressWidgetReceiver::class.java)
                        )?.contains(toDeleteWidgetSettings.widgetID) == false
                    ) {
                        val deleteWidgetDataDao = database?.getWidgetDataDao()
                        val deleteWidgetSettings = database?.getWidgetSettingsDao()
                        lifecycleScope.launch {
                            toDeleteWidgetData?.let {
                                deleteWidgetDataDao?.delete(toDeleteWidgetData)
                            }
                            toDeleteWidgetSettings?.let {
                                deleteWidgetSettings?.delete(toDeleteWidgetSettings)
                            }
                        }

                        // below line is to display our snackbar with action.
                        Snackbar.make(view, R.string.revert_delete, Snackbar.LENGTH_LONG)
                            .setAction(R.string.undo) { // adding on click listener to our action of snack bar.
                                lifecycleScope.launch {
                                    toDeleteWidgetData?.let {
                                        deleteWidgetDataDao?.insertUpdate(toDeleteWidgetData)
                                    }
                                    toDeleteWidgetSettings?.let {
                                        deleteWidgetSettings?.insertUpdate(
                                            toDeleteWidgetSettings
                                        )
                                    }
                                }
                            }
                            .show()
                    } else {
                        // cannot delete still have widget
                        Toast.makeText(
                            context, R.string.cannot_delete_still_widget, Toast.LENGTH_LONG
                        ).show()
                        listAdapter.notifyItemChanged(position)
                    }
                }
            }).attachToRecyclerView(addressListRecycler)

            val layoutManager = LinearLayoutManager(context)
            val dividerItemDecoration = DividerItemDecoration(
                context, layoutManager.orientation
            )

            addressListRecycler?.addItemDecoration(dividerItemDecoration)
            addressListRecycler?.layoutManager = layoutManager
        }
    }

    override fun onResume() {
        setUpRoomsToRecyclerListener()
        super.onResume()
        activity?.let {
            val supportActionBar = (activity as AppCompatActivity).supportActionBar
            supportActionBar?.setDisplayShowHomeEnabled(false)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }
    }

    @SuppressLint("CheckResult")
    private fun addNewAddress() {
        // open address popup
        this.context?.let { context ->
            MaterialDialog(context).show {
                input(
                    hintRes = R.string.public_address_hint, waitForPositiveButton = false
                ) { dialog, text ->
                    val textString = text.toString()
                    val inputField = dialog.getInputField()
                    if (ForkHelper.isChiaOrForkAddressValid(textString.trim())) {
                        dialog.setActionButtonEnabled(WhichButton.POSITIVE, true)
                    } else {
                        inputField.error = getString(R.string.chia_address_input_error_wrong)
                        dialog.setActionButtonEnabled(WhichButton.POSITIVE, false)
                    }
                }
                positiveButton(R.string.add_address) { dialog ->
                    getDataAndSaveAddress(dialog.getInputField().text.toString().trim())
                }
            }
        }
    }

    private fun getDataAndSaveAddress(address: String) {
        val context = this.context
        if (context != null) {
            this.lifecycleScope.launch {
                loadingSpinner?.visibility = View.VISIBLE
                val dataDao = database?.getWidgetDataDao()
                val widgetData = AllTheBlocksApiHelper.receiveWidgetDataFromApi(address)

                if (widgetData != null) {
                    dataDao?.insertUpdate(widgetData)
                } else {
                    Log.e(TAG, "Error connecting to server, or wrong response.")
                    MaterialDialog(context).show {
                        title(R.string.error)
                        message(R.string.server_communication_error)
                        icon(R.drawable.baseline_error_18)
                    }
                }
                loadingSpinner?.visibility = View.GONE
            }
        }
    }

    private fun setUpRoomsToRecyclerListener() {
        database?.getWidgetSettingsAndDataDao()?.loadAllLiveData()?.observe(viewLifecycleOwner) {
            addressListRecycler?.adapter = ChiaAddressListAdapter(it)
        }
    }
}
