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
    val ALL_THE_BLOCKS_CURRENCIES = mapOf(
        "xch" to CoinInfo("chia", "XCH", "Chia"),
        "xcc" to CoinInfo("chives", "XCC", "Chives"),
        "ach" to CoinInfo("achi", "ACH", "Achi"),
        "arc" to CoinInfo("aedge", "AEC", "AedgeCoin"),
        "apple" to CoinInfo("apple", "APPLE", "Apple"),
        "avo" to CoinInfo("avocado", "AVO", "Avocado"),
        "xbr" to CoinInfo("beer", "XBR", "Beer"),
        "xbt" to CoinInfo("beet", "XBT", "Beet"),
        "xbtc" to CoinInfo("btcgreen", "XBTC", "BTCGreen"),
        "vag" to CoinInfo("c_nt", "VAG", "CuntCoin"),
        "cac" to CoinInfo("cactus", "CAC", "Cactus"),
        "cans" to CoinInfo("cannabis", "CANS", "Cannabis"),
        "cgn" to CoinInfo("chaingreen", "XGN", "Chaingreen"),
        "cov" to CoinInfo("covid", "COV", "Covid"),
        "xcd" to CoinInfo("cryptodoge", "XCD", "CryptoDoge"),
        "xdg" to CoinInfo("dogechia", "XDG", "DogeChia"),
        "xeq" to CoinInfo("equality", "XEQ", "Equality"),
        "ffk" to CoinInfo("fishery", "FKK", "Fishery"),
        "xfx" to CoinInfo("flax", "XFX", "Flax"),
        "xfl" to CoinInfo("flora", "XFL", "Flora"),
        "xfk" to CoinInfo("fork", "XFK", "Fork"),
        "xgj" to CoinInfo("goji", "XGJ", "Goji"),
        "ozt" to CoinInfo("goldcoin", "OZT", "Goldcoin"),
        "gdog" to CoinInfo("greendoge", "GDOG", "GreenDoge"),
        "hdd" to CoinInfo("hddcoin", "HDD", "HDDCoin"),
        "xka" to CoinInfo("kale", "XKA", "Kale"),
        "xkw" to CoinInfo("kiwi", "XKW", "Kiwi"),
        "xkj" to CoinInfo("kujenga", "XKJ", "Kujenga"),
        "llc" to CoinInfo("littlelambocoin", "LCC", "LittleLamboCoin"),
        "lch" to CoinInfo("lotus", "LCH", "Lotus"),
        "six" to CoinInfo("lucky", "SIX", "Lucky"),
        "xmz" to CoinInfo("maize", "XMZ", "Maize"),
        "xmx" to CoinInfo("melati", "XMX", "Melati"),
        "melon" to CoinInfo("melon", "MELON", "mELON"),
        "xkm" to CoinInfo("mint", "XKM", "Mint"),
        "mga" to CoinInfo("mogua", "MGA", "Mogua"),
        "nch" to CoinInfo("nchain", "NCH", "N-Chain"),
        "xol" to CoinInfo("olive", "XOL", "Olive"),
        "pea" to CoinInfo("peas", "PEA", "Peass"),
        "pips" to CoinInfo("pipscoin", "PIPS", "Pipscoin"),
        "xcr" to CoinInfo("rose", "XCR", "Rose"),
        "xslv" to CoinInfo("salvia", "XLSV", "Salvia"),
        "scm" to CoinInfo("scam", "SCM", "Scam"),
        "xsc" to CoinInfo("sector", "XSC", "Sector"),
        "xse" to CoinInfo("seno", "XSE", "Seno"),
        "xnt" to CoinInfo("skynet", "XNT", "Skynet"),
        "sock" to CoinInfo("socks", "SOCK", "Socks"),
        "spare" to CoinInfo("spare", "SPARE", "Spare"),
        "stai" to CoinInfo("stai", "STAI", "STAI"),
        "stor" to CoinInfo("stor", "STOR", "STOR"),
        "xtx" to CoinInfo("taco", "XTX", "Taco"),
        "tad" to CoinInfo("tad", "TAD", "Tad"),
        "xth" to CoinInfo("thyme", "XTH", "Thyme"),
        "trz" to CoinInfo("tranzact", "TRZ", "Tranzact"),
        "xvm" to CoinInfo("venidium", "XVM", "Venidium"),
        "wheat" to CoinInfo("wheat", "WHEAT", "Wheat"),
        "xca" to CoinInfo("xcha", "XCA", "Xcha")
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
