package ninja.bored.chiapublicaddressmonitor.model

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kaspersky.kaspresso.annotations.ScreenShooterTest
import com.kaspersky.kaspresso.testcases.api.testcase.DocLocScreenshotTestCase
import java.io.File
import ninja.bored.chiapublicaddressmonitor.MainActivity
import ninja.bored.chiapublicaddressmonitor.model.screens.AddressDetailScreen
import ninja.bored.chiapublicaddressmonitor.model.screens.AddressListRecyclerItem
import ninja.bored.chiapublicaddressmonitor.model.screens.AddressListScreen
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChiaWidgetScreenshots : DocLocScreenshotTestCase(
    screenshotsDirectory = File("Download/screenshots"),
    locales = "en"
) {

    // @get:Rule
    // val activityTestRule = ActivityTestRule(MainActivity::class.java, true, false)

    @ScreenShooterTest
    @Test
    fun test() = run {
        var stepCounter = 0
        AddressListScreen {
            step("${++stepCounter}. Prestart") {
                captureScreenshot(it.description)
            }
            step("${++stepCounter}. Launch activity") {
                ActivityScenario.launch(MainActivity::class.java)
                captureScreenshot(it.description)
            }

            step("${++stepCounter}. click new button") {
                newButton.click()
                captureScreenshot(it.description)
            }

            step("${++stepCounter}. enter address") {
                editText.replaceText("xch1away45w2acy8cqgcjxnne8aket33y49tt437gjjk86y7fanstw7qyewsrf")
                captureScreenshot(it.description)
            }

            step("${++stepCounter}. click add Button") {
                positiveButton.click()
                captureScreenshot(it.description)
            }

            addressRecycler.firstChild<AddressListRecyclerItem> {
                step("${++stepCounter}. waited for recycler first item") {
                    captureScreenshot(it.description)
                }
                step("${++stepCounter}. click first item") {
                    this.click()
                    captureScreenshot(it.description)
                }
            }
        }
        // Detail page
        AddressDetailScreen {
            step("${++stepCounter}. type address name") {
                chiaAddressSynonymTextInputEditText.typeText("Pool Farmer")
                captureScreenshot(it.description)
            }
            step("${++stepCounter}. check notifications") {
                addressHasNotification.click()
                captureScreenshot(it.description)
            }
            step("${++stepCounter}. check use gross balance") {
                useGrossBalance.click()
                captureScreenshot(it.description)
            }

            chiaConvertionSpinner {
                step("${++stepCounter}. open Spinner") {
                    open()
                    captureScreenshot(it.description)
                }
//                step("${++stepCounter}. choose spinner item") {
//                    childAt<KSpinnerItem>(2) {
//                        this.click()
//                        captureScreenshot(it.description)
//                    }
//                }
            }
        }
    }
}
