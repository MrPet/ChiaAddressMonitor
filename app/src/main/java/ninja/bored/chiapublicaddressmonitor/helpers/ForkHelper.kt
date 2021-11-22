package ninja.bored.chiapublicaddressmonitor.helpers

import ninja.bored.chiapublicaddressmonitor.model.CoinInfo

object ForkHelper {
    /**
     * validate chia or fork address
     */
    fun isChiaOrForkAddressValid(address: String?): Boolean {
        address?.let {
            val currencyPrefix = getCurrencyAddressPrefix(address)
            val checkRegex =
                Regex("^$currencyPrefix[\\d\\w]{${Constants.FORK_ADDRESS_LENGTH_WITHOUT_PREFIX}}$")
            if (checkRegex.matches(address)) {
                return true
            }
        }
        return false
    }

    fun getCurrencyIdentifierFromAddress(address: String?): String? {
        return getCoinClassFromAddress(address)?.allTheBlocksCoinUrlShort
    }

    fun getCurrencySymbolFromAddress(address: String?): String? {
        return getCoinClassFromAddress(address)?.coinCurrencySymbol
    }

    fun getCurrencyDisplayNameFromAddress(address: String?): String? {
        return getCoinClassFromAddress(address)?.coinDisplayName
    }

    fun getNetBalanceDividerFromAddress(address: String?): Double? {
        return Constants.ALL_THE_BLOCKS_CURRENCIES[getCurrencyAddressPrefix(address)]?.netBalanceDivider
    }

    private fun getCoinClassFromAddress(address: String?): CoinInfo? {
        return Constants.ALL_THE_BLOCKS_CURRENCIES[getCurrencyAddressPrefix(address)]
    }

    fun getCurrencyAddressPrefix(address: String?): Any? {
        address?.let {
            val addressIndex = address.indexOf("1")
            if (addressIndex >= 0) {
                return address.substring(0, addressIndex)
            }
        }
        return null
    }
}
