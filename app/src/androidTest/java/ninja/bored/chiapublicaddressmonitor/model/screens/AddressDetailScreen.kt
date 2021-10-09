package ninja.bored.chiapublicaddressmonitor.model.screens

import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.spinner.KSpinner
import io.github.kakaocup.kakao.spinner.KSpinnerItem
import io.github.kakaocup.kakao.switch.KSwitch
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import ninja.bored.chiapublicaddressmonitor.R

object AddressDetailScreen : KScreen<AddressDetailScreen>() {
    override val layoutId: Int = R.layout.fragment_address_details
    override val viewClass: Class<*>? = null

    val addressDetailFragmentHeader = KTextView { withId(R.id.address_detail_fragment_header) }
    val copyAddressButton = KButton { withId(R.id.copy_address_button) }
    val addressHasNotification = KSwitch { withId(R.id.address_has_notification) }

    // val useGrossBalance = KSwitch { withId(R.id.use_gross_balance) }
    val chiaConvertionSpinner =
        KSpinner(builder = { withId(R.id.chia_convertion_spinner) }, itemTypeBuilder = {
            itemType(::KSpinnerItem)
        })
    val chiaAddressSynonymTextInputEditText =
        KEditText { withId(R.id.chia_address_synonym_text_input_edit_text) }
    val saveButton = KButton { withId(R.id.save_address_settings_button) }
}
