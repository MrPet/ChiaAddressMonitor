package ninja.bored.chiapublicaddressmonitor.adapter

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import ninja.bored.chiapublicaddressmonitor.AddressDetailsFragment
import ninja.bored.chiapublicaddressmonitor.ChiaPublicAddressWidgetReceiver
import ninja.bored.chiapublicaddressmonitor.R
import ninja.bored.chiapublicaddressmonitor.helpers.Constants
import ninja.bored.chiapublicaddressmonitor.helpers.Slh
import ninja.bored.chiapublicaddressmonitor.model.WidgetData
import ninja.bored.chiapublicaddressmonitor.model.WidgetSettingsAndData
import java.text.DateFormat

class ChiaAddressListAdapter(private val widgetSettingsAndData: List<WidgetSettingsAndData>) :
    RecyclerView.Adapter<ChiaAddressListViewHolder>() {

    override fun onBindViewHolder(holder: ChiaAddressListViewHolder, position: Int) {
        holder.bindData(widgetSettingsAndData[position])
    }

    fun getData() = widgetSettingsAndData

    override fun getItemCount() = widgetSettingsAndData.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChiaAddressListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_chia_address_list, parent, false)
        return ChiaAddressListViewHolder(view)
    }

    fun getDataFromPosition(position: Int): WidgetSettingsAndData {
        return widgetSettingsAndData[position]
    }
}

class ChiaAddressListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var itemAmountAndDateTextView: TextView? =
        itemView.findViewById(R.id.recyclerItemAmountAndDate)
    private var chiaAddressTextView: TextView? = itemView.findViewById(R.id.recyclerItemChiaAddress)
    private var recyclerItemHeader: TextView? = itemView.findViewById(R.id.recyclerItemHeader)
    private var addWidgetButton: ImageButton? = itemView.findViewById(R.id.addWidgetButton)
    private var notificationImageView: ImageView? =
        itemView.findViewById(R.id.notificationsIconImageView)

    fun bindData(widgetSettingsAndData: WidgetSettingsAndData) {
        chiaAddressTextView?.text = widgetSettingsAndData.widgetData?.chiaAddress
        val localDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
        val context = chiaAddressTextView?.context
        if (widgetSettingsAndData.widgetData?.updateDate != null) {
            val chiaAmount = when (widgetSettingsAndData.addressSettings?.useGrossBalance)
            {
                true -> widgetSettingsAndData.widgetData.chiaGrossAmount
                else -> widgetSettingsAndData.widgetData.chiaAmount
            }
            itemAmountAndDateTextView?.text = context?.getString(
                R.string.recycler_item_amount_and_date,
                Slh.formatChiaDecimal(
                    chiaAmount,
                    Constants.Precision.TOTAL
                ),
                localDateFormat.format(widgetSettingsAndData.widgetData.updateDate)
            )
        } else {
            itemAmountAndDateTextView?.text = context?.getString(R.string.loading)
        }

        widgetSettingsAndData.widgetData?.let {
            itemView.setOnClickListener {
                val newFragment =
                    AddressDetailsFragment.newInstance(widgetSettingsAndData.widgetData.chiaAddress)
                val transaction =
                    (context as AppCompatActivity).supportFragmentManager.beginTransaction()
                transaction.replace(R.id.nav_host_fragment, newFragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }

        widgetSettingsAndData.addressSettings?.chiaAddressSynonym?.let {
            recyclerItemHeader?.text = widgetSettingsAndData.addressSettings.chiaAddressSynonym
        }

        if (widgetSettingsAndData.addressSettings?.showNotification == true) {
            notificationImageView?.setImageResource(R.drawable.baseline_notifications_active_24)
        } else {
            notificationImageView?.setImageResource(R.drawable.baseline_notifications_off_24)
        }

        widgetSettingsAndData.widgetData?.chiaAddress?.let {
            addWidgetButton?.setOnClickListener {
                this.addWidget(widgetSettingsAndData.widgetData)
            }
        }
    }

    private fun addWidget(widgetData: WidgetData) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val clickContext = itemView.context
            val appWidgetManager: AppWidgetManager =
                clickContext.getSystemService(AppWidgetManager::class.java)
            val myProvider =
                ComponentName(clickContext, ChiaPublicAddressWidgetReceiver::class.java)

            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                val intent = Intent(clickContext, ChiaPublicAddressWidgetReceiver::class.java)
                intent.putExtra(
                    Constants.ADDRESS_EXTRA,
                    widgetData.chiaAddress
                )

                val successCallback: PendingIntent = PendingIntent.getBroadcast(
                    clickContext,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                appWidgetManager.requestPinAppWidget(myProvider, null, successCallback)
            } else {
                Toast.makeText(
                    clickContext,
                    R.string.launcher_does_not_allow_widget_from_app,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
