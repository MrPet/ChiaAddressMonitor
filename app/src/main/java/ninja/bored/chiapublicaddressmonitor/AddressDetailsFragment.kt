package ninja.bored.chiapublicaddressmonitor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import ninja.bored.chiapublicaddressmonitor.helpers.Constants
import ninja.bored.chiapublicaddressmonitor.model.AddressSettings
import ninja.bored.chiapublicaddressmonitor.model.ChiaWidgetRoomsDatabase

private const val ARG_CHIA_ADDRESS = "param1"

/**
 * A simple [Fragment] subclass.
 * Use the [AddressDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddressDetailsFragment : Fragment() {

    private var chiaAddress: String? = null
    private val database by lazy { this.context?.let { ChiaWidgetRoomsDatabase.getInstance(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { chiaAddress = it.getString(ARG_CHIA_ADDRESS) }
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            val supportActionBar = (activity as AppCompatActivity).supportActionBar
            supportActionBar?.setDisplayShowHomeEnabled(true)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_address_details, container, false)
        chiaAddress?.let { cChiaAddress ->
            database?.let { db ->
                this.lifecycleScope.launch {
                    val chiaAddressSettingsDao = db.getAddressSettingsDao()
                    var chiaAddressSettings = chiaAddressSettingsDao.getByAddress(cChiaAddress)
                    if (chiaAddressSettings == null) {
                        // no settings ...
                        chiaAddressSettings = AddressSettings(
                            cChiaAddress,
                            false,
                            null,
                            Constants.defaultUpdateTime
                        )
                        chiaAddressSettingsDao.insertUpdate(chiaAddressSettings)
                    }
                    updateUiWithSettings(chiaAddressSettings, rootView)
                }
            }
        }
        val saveAddressSettingsButton =
            rootView.findViewById<Button>(R.id.save_address_settings_button)
        saveAddressSettingsButton.setOnClickListener {
            saveSettingsFromParentView(rootView)
        }
        return rootView
    }

    private fun updateUiWithSettings(chiaAddressSettings: AddressSettings, rootView: View) {
        val addressHeader = rootView.findViewById<TextView>(R.id.address_detail_fragment_header)
        addressHeader?.text = chiaAddress

        val notificationCheckbox =
            rootView.findViewById<SwitchCompat>(R.id.address_has_notification)
        notificationCheckbox.isChecked = chiaAddressSettings.showNotification

        val addressSynonym =
            rootView.findViewById<TextInputEditText>(R.id.chia_address_synonym_text_input_edit_text)
        addressSynonym.setText(chiaAddressSettings.chiaAddressSynonym)
    }

    private fun saveSettingsFromParentView(parentView: View) {
        val fragment = this
        this.chiaAddress?.let { chiaAddress ->
            database?.let { db ->
                val notificationCheckbox =
                    parentView.findViewById<SwitchCompat>(R.id.address_has_notification)
                val addressSynonym =
                    parentView.findViewById<TextInputEditText>(R.id.chia_address_synonym_text_input_edit_text)
                val addressSynonymString = when (addressSynonym.text.toString().trim()) {
                    "" -> null
                    else -> addressSynonym.text.toString()
                }
                val currentAddressSettings = AddressSettings(
                    chiaAddress,
                    notificationCheckbox.isChecked,
                    addressSynonymString,
                    Constants.defaultUpdateTime
                )
                val chiaAddressSettingsDao = db.getAddressSettingsDao()
                this.lifecycleScope.launch {
                    chiaAddressSettingsDao.insertUpdate(currentAddressSettings)
                    Toast.makeText(
                        fragment.context,
                        R.string.saved_address_settings,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param chiaAddress
         * @return A new instance of fragment AddressDetailsFragment.
         */
        @JvmStatic fun newInstance(chiaAddress: String) =
            AddressDetailsFragment().apply {
                arguments = Bundle().apply { putString(ARG_CHIA_ADDRESS, chiaAddress) }
            }
    }
}
