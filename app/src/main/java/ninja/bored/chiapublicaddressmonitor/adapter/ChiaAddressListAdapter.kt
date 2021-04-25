package ninja.bored.chiapublicaddressmonitor.adapter

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import ninja.bored.chiapublicaddressmonitor.ChiaPublicAddressWidgetReceiver
import ninja.bored.chiapublicaddressmonitor.R
import ninja.bored.chiapublicaddressmonitor.helpers.Constants
import ninja.bored.chiapublicaddressmonitor.model.WidgetSettingsAndData
import java.text.DateFormat

class ChiaAddressListAdapter(private val widgetSettingsAmdData: List<WidgetSettingsAndData>) :
    RecyclerView.Adapter<ChiaAddressListViewHolder>() {

    override fun onBindViewHolder(holder: ChiaAddressListViewHolder, position: Int) {
        holder.bindData(widgetSettingsAmdData[position])
    }

    fun getData() = widgetSettingsAmdData

    override fun getItemCount() = widgetSettingsAmdData.count()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChiaAddressListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_chia_address_list, parent, false)
        return ChiaAddressListViewHolder(view)
    }

    fun getDataFromPosition(position: Int): WidgetSettingsAndData {
        return widgetSettingsAmdData[position]
    }
}

class ChiaAddressListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var itemAmountAndDateTextView: TextView? =
        itemView.findViewById(R.id.recyclerItemAmountAndDate)
    private var chiaAddressTextView: TextView? = itemView.findViewById(R.id.recyclerItemChiaAddress)

    fun bindData(widgetSettingsAndData: WidgetSettingsAndData) {
        chiaAddressTextView?.text = widgetSettingsAndData.widgetData?.chiaAddress
        val localDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
        val context = chiaAddressTextView?.context
        if (widgetSettingsAndData.widgetData?.updateDate != null) {
            itemAmountAndDateTextView?.text = context?.getString(
                R.string.recycler_item_amount_and_date,
                widgetSettingsAndData.widgetData.chiaAmount,
                localDateFormat.format(widgetSettingsAndData.widgetData.updateDate)
            )
        } else {
            itemAmountAndDateTextView?.text = context?.getString(R.string.loading)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && widgetSettingsAndData.widgetData?.chiaAddress != null) {
            itemView.setOnClickListener {
                val clickContext = itemView.context

                val appWidgetManager: AppWidgetManager =
                    clickContext.getSystemService(AppWidgetManager::class.java)
                val myProvider =
                    ComponentName(clickContext, ChiaPublicAddressWidgetReceiver::class.java)

                if (appWidgetManager.isRequestPinAppWidgetSupported) {
                    val intent = Intent(context, ChiaPublicAddressWidgetReceiver::class.java)
                    intent.putExtra(
                        Constants.ADDRESS_EXTRA,
                        widgetSettingsAndData.widgetData.chiaAddress
                    )

                    val successCallback: PendingIntent = PendingIntent.getBroadcast(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )

                    appWidgetManager.requestPinAppWidget(myProvider, null, successCallback)
                } else {
                    Toast.makeText(
                        context,
                        R.string.launcher_does_not_allow_widget_from_app,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
