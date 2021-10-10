package ninja.bored.chiapublicaddressmonitor.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import ninja.bored.chiapublicaddressmonitor.R
import ninja.bored.chiapublicaddressmonitor.model.WidgetData
import ninja.bored.chiapublicaddressmonitor.model.WidgetSettingsAndData

class ChiaAddressMultiSelectListAdapter(widgetSettingsAndData: List<WidgetSettingsAndData>) :
    ChiaAddressListAdapter(widgetSettingsAndData) {
    val selectedAddressesList: ArrayList<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChiaAddressListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item_chia_address_select_list, parent, false)
        return ChiaAddressListViewHolder(view)
    }

    override fun setUpOnItemClickListener(
        widgetData: WidgetData,
        context: Context,
        holder: ChiaAddressListViewHolder
    ) {
        holder.itemView.setOnClickListener {
            it.isSelected = !it.isSelected
            if (it.isSelected) {
                it.setBackgroundColor(context.getColor(R.color.chia))
                selectedAddressesList.add(widgetData.chiaAddress)
            } else {
                it.background = CardView(context).background
                selectedAddressesList.remove(widgetData.chiaAddress)
            }
        }
    }
}
