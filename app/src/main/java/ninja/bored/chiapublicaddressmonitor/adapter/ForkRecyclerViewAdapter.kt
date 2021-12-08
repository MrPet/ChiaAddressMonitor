package ninja.bored.chiapublicaddressmonitor.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ninja.bored.chiapublicaddressmonitor.databinding.FragmentForkBinding
import ninja.bored.chiapublicaddressmonitor.helpers.Constants
import ninja.bored.chiapublicaddressmonitor.model.CoinInfo

class ForkRecyclerViewAdapter(
    private val originalValues: List<CoinInfo>
) : RecyclerView.Adapter<ForkRecyclerViewAdapter.ViewHolder>(), Filterable {
    private var filteredValues: List<CoinInfo> = originalValues

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentForkBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredValues[position]
        holder.contentView.text = "${item.coinDisplayName} (${item.coinCurrencySymbol})"
        holder.coinLineHolder.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(Constants.BASE_ALL_THE_BLOCKS_URL + item.allTheBlocksCoinUrlShort)
            )
            holder.contentView.context?.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = filteredValues.size

    inner class ViewHolder(binding: FragmentForkBinding) : RecyclerView.ViewHolder(binding.root) {
        val contentView: TextView = binding.content
        val coinLineHolder: View = binding.root

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults().apply {
                    values = when (constraint) {
                        null -> originalValues
                        "" -> originalValues
                        else -> originalValues.filter { coinInfo ->
                            coinInfo.coinDisplayName.contains(constraint.toString(), true) ||
                                coinInfo.coinCurrencySymbol.contains(
                                    constraint.toString(),
                                    true
                                )
                        }
                    }
                }
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredValues = results?.values as List<CoinInfo>
                notifyDataSetChanged()
            }
        }
    }
}
