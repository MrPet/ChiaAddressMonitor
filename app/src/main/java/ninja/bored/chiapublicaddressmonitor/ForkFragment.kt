package ninja.bored.chiapublicaddressmonitor

import HeaderDecoration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
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
        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        val searchBar = view.findViewById<SearchView>(R.id.fork_search_bar)
        // Set the adapter

        with(recyclerView) {
            addItemDecoration(
                HeaderDecoration(context, recyclerView, R.layout.fork_list_header)
            )
            layoutManager = LinearLayoutManager(context)
            adapter = ForkRecyclerViewAdapter(
                Constants.ALL_THE_BLOCKS_CURRENCIES.values.sortedBy { it.coinDisplayName }.toList()
            )

            searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    val adapterHelper = adapter
                    if (adapterHelper is ForkRecyclerViewAdapter) {
                        adapterHelper.filter.filter(newText)
                    }
                    return false
                }
            })
        }
        return view
    }
}
