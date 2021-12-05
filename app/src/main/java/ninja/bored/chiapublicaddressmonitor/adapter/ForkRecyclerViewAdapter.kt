package ninja.bored.chiapublicaddressmonitor.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ninja.bored.chiapublicaddressmonitor.databinding.FragmentForkBinding
import ninja.bored.chiapublicaddressmonitor.helpers.Constants
import ninja.bored.chiapublicaddressmonitor.model.CoinInfo


class ForkRecyclerViewAdapter(
    private val values: List<CoinInfo>
) : RecyclerView.Adapter<ForkRecyclerViewAdapter.ViewHolder>() {

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
        val item = values[position]
        holder.contentView.text = "${item.coinDisplayName} (${item.coinCurrencySymbol})"
        holder.coinLineHolder.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse(Constants.BASE_ALL_THE_BLOCKS_URL + item.allTheBlocksCoinUrlShort)
            )
            if (intent.resolveActivity(holder.contentView.context.packageManager) != null) {
                holder.contentView.context?.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentForkBinding) : RecyclerView.ViewHolder(binding.root) {
        val contentView: TextView = binding.content
        val coinLineHolder: View = binding.root

        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }
}
