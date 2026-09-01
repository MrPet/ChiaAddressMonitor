@file:Suppress("MagicNumber")

package ninja.bored.chiapublicaddressmonitor.helpers

import ninja.bored.chiapublicaddressmonitor.BuildConfig
import ninja.bored.chiapublicaddressmonitor.model.CoinInfo
import ninja.bored.chiapublicaddressmonitor.model.CurrencyConversionInfo

object Constants {
    const val TIME_THRESHOLD_FOR_FIAT_CONVERSION: Int = 15
    const val BIG_AMOUNT_THRESHOLD = 10000
    const val defaultUpdateTime: Int = 3600
    const val NOTIFICATION_CHANNEL_POSITIVE_CHANGE: String = "positiveChange"

    // for updating the notifications
    const val NOTIFICATION_ID_POSITIVE_CHANGE: Int = 13371
    const val NOTIFICATION_ID_NEGATIVE_CHANGE: Int = 13372
    const val NOTIFICATION_CHANNEL_NEGATIVE_CHANGE: String = "negativeChange"
    const val ADDRESS_NOT_FOUND_HTTP_CODE: Int = 404
    const val STATE_OK_CODE: Int = 200
    const val NET_BALANCE_DIVIDER: Double = 1000000000000.0
    const val ADDRESS_EXTRA: String = "CHIA_ADDRESS"
    const val CHIA_ADDRESS_LENGTH: Int = 62
    const val CHIA_ADDRESS_PREFIX: String = "xch"
    const val FORK_ADDRESS_LENGTH_WITHOUT_PREFIX: Int = 62 - CHIA_ADDRESS_PREFIX.length
    const val SHORT_DATE_TIME_FORMAT: String = "EEE, HH:mm"
    const val BASE_ALL_THE_BLOCKS_API_URL: String = "https://api.alltheblocks.net/"
    const val BASE_ALL_THE_BLOCKS_URL: String = "https://alltheblocks.net/"
    val ALL_THE_BLOCKS_CURRENCIES = mapOf(
        "aba" to CoinInfo("aba", "ABA", "Aba", 1000000000000.0),
        "ach" to CoinInfo("achi", "ACH", "Achi", 1000000000.0),
        "apple" to CoinInfo("apple", "APPLE", "Apple", 1000000000000.0),
        "ball" to CoinInfo("ballcoin", "BALL", "Ballcoin", 1000000000000.0),
        "cac" to CoinInfo("cactus", "CAC", "Cactus", 1000000000000.0),
        "xch" to CoinInfo("chia", "XCH", "Chia", 1000000000000.0),
        "txch" to CoinInfo("testnet11", "TXCH", "Testnet11", 1000000000000.0),
        "xck" to CoinInfo("chik", "XCK", "Chik", 1000000000000.0),
        "xcc" to CoinInfo("chives", "XCC", "Chives", 100000000.0),
        "xfx" to CoinInfo("flax", "XFX", "Flax", 1000000000000.0),
        "gl" to CoinInfo("gold", "GL", "Gold", 1000000000000.0),
        "gbtc" to CoinInfo("greenbtc", "GBTC", "GreenBTC", 1000000000000.0),
        "hdd" to CoinInfo("hddcoin", "HDD", "HDDcoin", 1000000000000.0),
        "moc" to CoinInfo("moon", "MOC", "Moon", 1000000000000.0),
        "ssd" to CoinInfo("ssdcoin", "SSD", "SSDCoin", 1000000000000.0),
        "stai" to CoinInfo("stai", "STAI", "STAI", 1000000000.0),
        "wheat" to CoinInfo("wheat", "WHEAT", "Wheat", 1000000000000.0),
        "xxch" to CoinInfo("xxch", "XXCH", "XXCH", 1000000000000.0)
    )

    const val BASE_ALL_THE_BLOCKS_API_ADDRESS_PATH: String = "/address/name/"
    const val BASE_API_URL: String = "https://public-api.chiaexplorer.com/0.1/balance/"
    const val CHIA_EXPLORER_API_KEY_HEADER_NAME: String = "x-api-key"
    const val CHIA_EXPLORER_API_KEY: String =
        "NOT used anymore" // BuildConfig.CHIA_EXPLORER_API_KEY
    const val CHIA_CONVERSIONS_BASE_API_URL: String = "https://cmccache.bored.ninja/price"
    const val CHIA_CONVERSIONS_API_KEY_HEADER_NAME: String = "api-key"
    const val CHIA_CONVERSIONS_API_KEY: String = BuildConfig.CHIA_CONVERSIONS_API_KEY

    object Precision {
        const val TOTAL = "TOTAL"
        const val MOJO = "MOJO"
        const val NORMAL = "NORMAL"
        const val FIAT = "FIAT"
    }

    object CurrencyCode {
        const val XCH = "XCH"
        const val USD = "USD"
        const val EUR = "EUR"
        const val MOJO = "MOJO"
    }

    val CHIA_CURRENCY_CONVERSIONS: Map<String, CurrencyConversionInfo> = mapOf(
        CurrencyCode.XCH to CurrencyConversionInfo(CurrencyCode.XCH, Precision.NORMAL, 1.0),
        CurrencyCode.MOJO to CurrencyConversionInfo(
            CurrencyCode.MOJO,
            Precision.MOJO,
            NET_BALANCE_DIVIDER
        ),
        CurrencyCode.EUR to CurrencyConversionInfo(CurrencyCode.EUR, Precision.FIAT, null),
        CurrencyCode.USD to CurrencyConversionInfo(CurrencyCode.USD, Precision.FIAT, null)
    )

    const val UPDATE_WORKER_INTERVAL_IN_MINUTES: Long = 60L
    const val UPDATE_WORKER_NAME: String = "widget_update_worker"
}
