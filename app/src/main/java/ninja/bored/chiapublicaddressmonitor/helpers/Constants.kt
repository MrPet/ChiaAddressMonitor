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
        "xch" to CoinInfo("chia", "XCH", "Chia", 1000000000000.0),
        "xfx" to CoinInfo("flax", "XFX", "Flax", 1000000000000.0),
        "cgn" to CoinInfo("chaingreen", "CGN", "Chaingreen", 1000000000000.0),
        "spare" to CoinInfo("spare", "SPARE", "Spare", 1000000000000.0),
        "xgj" to CoinInfo("goji", "XGJ", "Goji", 1000000000000.0),
        "xfl" to CoinInfo("flora", "XFL", "Flora", 1000000000000.0),
        "xse" to CoinInfo("seno", "XSE", "Seno", 1000000000000.0),
        "xcr" to CoinInfo("rose", "XCR", "Rose", 1000000000.0),
        "hdd" to CoinInfo("hddcoin", "HDD", "HDDcoin", 1000000000000.0),
        "xdg" to CoinInfo("dogechia", "XDG", "DogeChia", 1000000000000.0),
        "avo" to CoinInfo("avocado", "AVO", "Avocado", 1000000000000.0),
        "xcd" to CoinInfo("cryptodoge", "XCD", "CryptoDoge", 1000000.0),
        "xka" to CoinInfo("kale", "XKA", "Kale", 1000000000000.0),
        "gdog" to CoinInfo("greendoge", "GDOG", "GreenDoge", 1000000000000.0),
        "xcc" to CoinInfo("chives", "XCC", "Chives", 100000000.0),
        "xmx" to CoinInfo("melati", "XMX", "Melati", 1000000000000.0),
        "xtx" to CoinInfo("taco", "XTX", "Taco", 1000000000000.0),
        "wheat" to CoinInfo("wheat", "WHEAT", "Wheat", 1000000000000.0),
        "sock" to CoinInfo("socks", "SOCK", "Socks", 1000000000000.0),
        "cac" to CoinInfo("cactus", "CAC", "Cactus", 1000000000000.0),
        "sit" to CoinInfo("silicoin", "SIT", "Silicoin", 1000000000000.0),
        "xsc" to CoinInfo("sector", "XSC", "Sector", 1000000000000.0),
        "tad" to CoinInfo("tad", "TAD", "Tad", 1000000000000.0),
        "apple" to CoinInfo("apple", "APPLE", "Apple", 1000000000000.0),
        "cans" to CoinInfo("cannabis", "CANS", "Cannabis", 1000000000000.0),
        "xmz" to CoinInfo("maize", "XMZ", "Maize", 1000000000000.0),
        "xfk" to CoinInfo("fork", "XFK", "Fork", 1000000000000.0),
        "cov" to CoinInfo("covid", "COV", "Covid", 1000000000000.0),
        "xbtc" to CoinInfo("btcgreen", "XBTC", "BTCgreen", 1000000000000.0),
        "nch" to CoinInfo("nchain", "NCH", "N-Chain", 1000000000000.0),
        "scm" to CoinInfo("scam", "SCM", "Scam", 1000000000000.0),
        "vag" to CoinInfo("c_nt", "VAG", "CuntCoin", 1000000000000.0),
        "ffk" to CoinInfo("fishery", "FFK", "Fishery", 1000000000.0),
        "xol" to CoinInfo("olive", "XOL", "Olive", 1000000000000.0),
        "six" to CoinInfo("lucky", "SIX", "Lucky", 1000000000000.0),
        "ach" to CoinInfo("achi", "ACH", "Achi", 1000000000.0),
        "pips" to CoinInfo("pipscoin", "PIPS", "Pipscoin", 1000000000000.0),
        "xbr" to CoinInfo("beer", "XBR", "Beer", 1000000000000.0),
        "xth" to CoinInfo("thyme", "XTH", "Thyme", 1000000000000.0),
        "xca" to CoinInfo("xcha", "XCA", "Xcha", 1000000000000.0),
        "stor" to CoinInfo("stor", "STOR", "Stor", 1000000000000.0),
        "ozt" to CoinInfo("goldcoin", "OZT", "Goldcoin", 1000000000000.0),
        "xbt" to CoinInfo("beet", "XBT", "Beet", 1000000000000.0),
        "xkw" to CoinInfo("kiwi", "XKW", "Kiwi", 1000000000000.0),
        "lch" to CoinInfo("lotus", "LCH", "Lotus", 1000000000000.0),
        "xkm" to CoinInfo("mint", "XKM", "Mint", 1000000000000.0),
        "mga" to CoinInfo("mogua", "MGA", "Mogua", 1000000000000.0),
        "trz" to CoinInfo("tranzact", "TRZ", "Tranzact", 1000000000000.0),
        "stai" to CoinInfo("stai", "STAI", "STAI", 1000000000.0),
        "xslv" to CoinInfo("salvia", "XSLV", "Salvia", 1000000000000.0),
        "pea" to CoinInfo("peas", "PEA", "Peas", 1000000000000.0),
        "melon" to CoinInfo("melon", "MELON", "mELON", 1000000000.0),
        "xkj" to CoinInfo("kujenga", "XKJ", "Kujenga", 1000000000000.0),
        "aec" to CoinInfo("aedge", "AEC", "AedgeCoin", 1000000000000.0),
        "xvm" to CoinInfo("venidium", "XVM", "Venidium", 1000000000000.0),
        "xnt" to CoinInfo("skynet", "XNT", "Skynet", 1000000000000.0),
        "xshib" to CoinInfo("shibgreen", "XSHIB", "SHIBgreen", 1000.0),
        "xeth" to CoinInfo("ethgreen", "XETH", "ETHgreen", 1000000000.0),
        "rolls" to CoinInfo("rolls", "ROLLS", "PecanRolls", 1000000000000.0),
        "llc" to CoinInfo("littlelambocoin", "LLC", "LittleLamboCoin", 1000.0)
    )
    const val BASE_ALL_THE_BLOCKS_API_ADDRESS_PATH: String = "/address/"
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

    const val UPDATE_WORKER_INTERVAL_IN_MINUTES: Long = 15L
    const val UPDATE_WORKER_NAME: String = "widget_update_worker"
}
