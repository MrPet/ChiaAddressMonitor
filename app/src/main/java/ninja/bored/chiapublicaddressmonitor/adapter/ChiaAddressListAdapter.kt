package ninja.bored.chiapublicaddressmonitor.adapter

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
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
import java.text.DateFormat
import ninja.bored.chiapublicaddressmonitor.AddressDetailsFragment
import ninja.bored.chiapublicaddressmonitor.ChiaPublicAddressWidgetReceiver
import ninja.bored.chiapublicaddressmonitor.R
import ninja.bored.chiapublicaddressmonitor.helpers.Constants
import ninja.bored.chiapublicaddressmonitor.helpers.Slh
import ninja.bored.chiapublicaddressmonitor.model.WidgetData
import ninja.bored.chiapublicaddressmonitor.model.WidgetSettingsAndData

open class ChiaAddressListAdapter(private val widgetSettingsAndData: List<WidgetSettingsAndData>) :
    RecyclerView.Adapter<ChiaAddressListViewHolder>() {

    override fun onBindViewHolder(holder: ChiaAddressListViewHolder, position: Int) {
        bindData(widgetSettingsAndData[position], holder)
    }

    private fun bindData(
        widgetSettingsAndData: WidgetSettingsAndData,
        holder: ChiaAddressListViewHolder
    ) {
        holder.chiaAddressTextView?.text = widgetSettingsAndData.widgetData?.chiaAddress
        val localDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
        val context = holder.chiaAddressTextView?.context
        if (widgetSettingsAndData.widgetData?.updateDate != null) {
            val chiaAmount = when (widgetSettingsAndData.addressSettings?.useGrossBalance) {
                true -> widgetSettingsAndData.widgetData.chiaGrossAmount
                else -> widgetSettingsAndData.widgetData.chiaAmount
            }
            holder.itemAmountAndDateTextView?.text = context?.getString(
                R.string.recycler_item_amount_and_date,
                Slh.formatChiaDecimal(
                    chiaAmount,
                    Constants.Precision.TOTAL
                ),
                Slh.getCurrencySymbolFromAddress(widgetSettingsAndData.addressSettings?.chiaAddress),
                localDateFormat.format(widgetSettingsAndData.widgetData.updateDate)
            )
        } else {
            holder.itemAmountAndDateTextView?.text = context?.getString(R.string.loading)
        }

        context?.let {
            widgetSettingsAndData.widgetData?.let { widgetData ->
                setUpOnItemClickListener(widgetData, context, holder)
            }
        }

        if (widgetSettingsAndData.addressSettings?.chiaAddressSynonym == null) {
            var coinName =
                Slh.getCurrencyDisplayNameFromAddress(widgetSettingsAndData.addressSettings?.chiaAddress)
            if (coinName == null) {
                coinName = holder.recyclerItemHeader?.context?.getString(R.string.unnknownCoin)
            }

            holder.recyclerItemHeader?.text = holder.recyclerItemHeader?.context?.getString(
                R.string.recycler_item_chia_address,
                coinName
            )
        } else {
            holder.recyclerItemHeader?.text =
                widgetSettingsAndData.addressSettings.chiaAddressSynonym
        }

        if (widgetSettingsAndData.addressSettings?.showNotification == true) {
            holder.notificationImageView?.setImageResource(R.drawable.baseline_notifications_active_24)
        } else {
            holder.notificationImageView?.setImageResource(R.drawable.baseline_notifications_off_24)
        }

        widgetSettingsAndData.widgetData?.chiaAddress?.let {
            holder.addWidgetButton?.setOnClickListener {
                this.addWidget(widgetSettingsAndData.widgetData, holder)
            }
        }
    }

    protected open fun setUpOnItemClickListener(
        widgetData: WidgetData,
        context: Context,
        holder: ChiaAddressListViewHolder
    ) {
        holder.itemView.setOnClickListener {
            val newFragment =
                AddressDetailsFragment.newInstance(widgetData.chiaAddress)
            val transaction =
                (context as AppCompatActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.nav_host_fragment, newFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    private fun addWidget(widgetData: WidgetData, holder: ChiaAddressListViewHolder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val clickContext = holder.itemView.context
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
                val flags = when {
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) -> {
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    }
                    else -> PendingIntent.FLAG_UPDATE_CURRENT
                }

                val successCallback: PendingIntent = PendingIntent.getBroadcast(
                    clickContext,
                    0,
                    intent,
                    flags
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

open class ChiaAddressListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var itemAmountAndDateTextView: TextView? =
        itemView.findViewById(R.id.recyclerItemAmountAndDate)
    var chiaAddressTextView: TextView? = itemView.findViewById(R.id.recyclerItemChiaAddress)
    var recyclerItemHeader: TextView? = itemView.findViewById(R.id.recyclerItemHeader)
    var addWidgetButton: ImageButton? = itemView.findViewById(R.id.addWidgetButton)
    var notificationImageView: ImageView? =
        itemView.findViewById(R.id.notificationsIconImageView)
}
