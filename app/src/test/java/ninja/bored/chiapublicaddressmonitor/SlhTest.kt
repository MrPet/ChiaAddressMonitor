@file:Suppress("SpellCheckingInspection")

package ninja.bored.chiapublicaddressmonitor

import com.google.gson.Gson
import junit.framework.TestCase
import ninja.bored.chiapublicaddressmonitor.helpers.Slh
import ninja.bored.chiapublicaddressmonitor.model.ChiaExplorerAddressResponse
import ninja.bored.chiapublicaddressmonitor.model.WidgetData
import org.junit.Test
import java.util.Date
import ninja.bored.chiapublicaddressmonitor.helpers.ChiaExplorerApiHelper

class SlhTest : TestCase() {
    @Test
    fun testChiaAddressValidityInvalidInput() {
        assertFalse("Address Null - invalid", Slh.isChiaAddressValid(null))
        assertFalse("Address Empty - invalid", Slh.isChiaAddressValid(""))
        assertFalse("Address Empty spaces - invalid", Slh.isChiaAddressValid("   "))
        assertFalse(
            "Address wrong starting characters - invalid",
            Slh.isChiaAddressValid("fxh1xntpeve5yjnadgjsyhc2szvjw07xt6mkv7d2v3qfvsvj097sywls7m6k2v")
        )
        assertFalse(
            "Address too long - invalid",
            Slh.isChiaAddressValid("xch1xntpeve5yjnadgjsyhc2szvjw07xt6mkv7d2v3qfvsvj097sywls7m6k2")
        )
        assertFalse(
            "Address too short - invalid",
            Slh.isChiaAddressValid("xch1xntpeve5yjnadgjsyhvjw07xt6mkv7d2v3qfvsvj097sywls7m6k2")
        )
        assertFalse(
            "Address invalid characters - invalid",
            Slh.isChiaAddressValid("xch1xntpeve5yjnadgjsyhc2szvjw07xt6mkv&d2v3qfvsvj097sywls7m6k2")
        )
    }

    @Test
    fun testpParseApiResponseToWidgetDataInvalid() {
        val address = "xch1xntpeve5yjnadgjsyhc2szvjw07xt6mkv&d2v3qfvsvj097sywls7m6k2"
        val date = Date()
        val highAmountChiaWidgetData = WidgetData(address, 18375000.0, date, 18475000.0)
        // maybe change datattypes in future, but for now it is good enough wont show so many decimals
        val highAmountParsedWidgetData = ChiaExplorerApiHelper.parseApiResponseToWidgetData(
            address,
            Gson().fromJson(
                "{\n" +
                "    \"grossBalance\": 18475000000000010000,\n" +
                "    \"netBalance\": 18375000000000010000\n" +
                "}",
                ChiaExplorerAddressResponse::class.java
            ),
            date
        )
        assertFalse(
            "Realy small difference compare " +
            "${highAmountChiaWidgetData.chiaAmount} == ${highAmountParsedWidgetData.chiaAmount}",
            highAmountChiaWidgetData.chiaAmount == highAmountParsedWidgetData.chiaAmount
        )
    }

    @Test
    fun testpParseApiResponseToWidgetData() {
        val address = "xch1xntpeve5yjnadgjsyhc2szvjw07xt6mkv&d2v3qfvsvj097sywls7m6k2"
        val zeroAmount = 0.0
        val date = Date()
        val chiaExplorerAddressResponse =
            ChiaExplorerAddressResponse(zeroAmount, zeroAmount)
        val chiaWidgetData = WidgetData(address, zeroAmount, date, zeroAmount)
        val parsedWidgetData =
            ChiaExplorerApiHelper.parseApiResponseToWidgetData(address, chiaExplorerAddressResponse, date)
        assertEquals(
            "Api Response Netbalance with Zero must match widget data initialized with zero",
            chiaWidgetData,
            parsedWidgetData
        )
        val highAmountChiaWidgetData = WidgetData(address, 18375000.0, date, 18475000.0)
        val highAmountParsedWidgetData = ChiaExplorerApiHelper.parseApiResponseToWidgetData(
            address,
            Gson().fromJson(
                "{\n" +
                "    \"grossBalance\": 18475000000000000000,\n" +
                "    \"netBalance\": 18375000000000000000\n" +
                "}",
                ChiaExplorerAddressResponse::class.java
            ),
            date
        )
        assertEquals(
            "High Api Response Netbalance must equal widget data initialisation",
            highAmountChiaWidgetData,
            highAmountParsedWidgetData
        )
        val decimalAmountChiaWidgetData = WidgetData(address, 28.1234, date, 28.2234)
        val smallAmountParsedWidgetData = ChiaExplorerApiHelper.parseApiResponseToWidgetData(
            address,
            Gson().fromJson(
                "{\n" +
                "    \"grossBalance\": 28223400000000,\n" +
                "    \"netBalance\": 28123400000000\n" +
                "}",
                ChiaExplorerAddressResponse::class.java
            ),
            date
        )
        assertEquals(
            "High Api Response Netbalance must equal widget data initialisation",
            decimalAmountChiaWidgetData,
            smallAmountParsedWidgetData
        )
    }

    @Test
    fun testChiaAddressValidityValidInput() {
        assertTrue(
            "Address is valid",
            Slh.isChiaAddressValid("xch16g76z3545xy2u4cgm52jyc7ymwyravn7m6unv9udfkvghreuuh7qa9cvfl")
        ) // chia network 1
        assertTrue(
            "Address is valid",
            Slh.isChiaAddressValid("xch1qhgp3ytyauptzyv5p48gnqpmkes6u2sf8llc7m3eurcpg3emg9yqzzptac")
        ) // donation address
    }
}
