package ninja.bored.chiapublicaddressmonitor.model.screens

import android.view.View
import com.kaspersky.kaspresso.screens.KScreen
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KButton
import ninja.bored.chiapublicaddressmonitor.R
import org.hamcrest.Matcher

class AddressListRecyclerItem(parent: Matcher<View>) :
    KRecyclerItem<AddressListRecyclerItem>(parent) {
    //    val holder = KView(parent) { withId(R.id.recyclerItemHolder) }
    //val title = KTextView(parent) { withId(R.id.recyclerItemHeader) }
//    val isNotificationEnableImageView =
//        KImageView(parent) { withId(R.id.notificationsIconImageView) }
//    val addWidgetButton = KButton(parent) { withId(R.id.addWidgetButton) }
//    val addressTextView = KTextView(parent) { withId(R.id.recyclerItemChiaAddress) }
//    val amountAndDateTextView = KTextView(parent) { withId(R.id.recyclerItemAmountAndDate) }
}

object AddressListScreen : KScreen<AddressListScreen>() {
    override val layoutId: Int = R.layout.fragment_address_list
    override val viewClass: Class<*>? = null

    val editText = KEditText { withId(com.afollestad.materialdialogs.input.R.id.md_input_message) }
    val positiveButton = KButton { withId(com.afollestad.materialdialogs.input.R.id.md_button_positive) }
    val newButton = KButton { withId(R.id.addAddressButton) }
    val addressRecycler: KRecyclerView =
        KRecyclerView(builder = { withId(R.id.address_list) }, itemTypeBuilder = {
            itemType(::AddressListRecyclerItem)
        })
}
