package ninja.bored.chiapublicaddressmonitor

import HeaderDecoration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ninja.bored.chiapublicaddressmonitor.adapter.ForkRecyclerViewAdapter
import ninja.bored.chiapublicaddressmonitor.helpers.Constants

/**
 * A fragment representing a list of Items.
 */
class ForkFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_fork_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                context?.let {
                    addItemDecoration(
                        HeaderDecoration.Builder(it).apply {
                            inflate(R.layout.fork_list_header)
                        }.build()
                    )
                }
                layoutManager = LinearLayoutManager(context)
                adapter =
                    ForkRecyclerViewAdapter(
                        Constants.ALL_THE_BLOCKS_CURRENCIES.values.sortedBy { it.coinDisplayName }
                            .toList()
                    )
            }
        }
        return view
    }
}
