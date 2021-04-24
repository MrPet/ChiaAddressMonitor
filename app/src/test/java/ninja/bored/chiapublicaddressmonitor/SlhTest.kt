@file:Suppress("SpellCheckingInspection")

package ninja.bored.chiapublicaddressmonitor

import junit.framework.TestCase
import ninja.bored.chiapublicaddressmonitor.helpers.Slh
import org.junit.Test

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
    fun testChiaAddressValidityValidInput() {
        assertTrue(
            "Address is valid",
            Slh.isChiaAddressValid("xch16g76z3545xy2u4cgm52jyc7ymwyravn7m6unv9udfkvghreuuh7qa9cvfl")
                  ) // chia network 1
        assertTrue(
            "Address is valid",
            Slh.isChiaAddressValid("xch1tyfk0mpw02kgcxuqx7f62l8v2juwa2ndtwgz7c4a37dctpnk66rqhugsg5")
                  ) // some big farmer
    }


}