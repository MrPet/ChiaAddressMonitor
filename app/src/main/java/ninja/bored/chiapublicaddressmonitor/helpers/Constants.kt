package ninja.bored.chiapublicaddressmonitor.helpers

import ninja.bored.chiapublicaddressmonitor.BuildConfig

object Constants {
    const val BIG_AMOUNT_THRESHOLD = 10000
    const val defaultUpdateTime: Int = 3600
    const val NOTIFICATION_CHANNEL_POSITIVE_CHANGE: String = "positiveChange"
    // for updating the notifications
    const val NOTIFICATION_ID_POSITIVE_CHANGE: Int = 13371
    const val NOTIFICATION_ID_NEGATIVE_CHANGE: Int = 13372
    const val NOTIFICATION_CHANNEL_NEGATIVE_CHANGE: String = "negativeChange"
    const val ADDRESS_NOT_FOUND_HTTP_CODE: Int = 404
    const val NET_BALANCE_DIVIDER: Double = 1000000000000.0
    const val ADDRESS_EXTRA: String = "CHIA_ADDRESS"
    const val CHIA_ADDRESS_LENGTH: Int = 62
    const val CHIA_ADDRESS_PREFIX: String = "xch"
    const val SHORT_DATE_TIME_FORMAT: String = "EEE, HH:mm"
    const val BASE_API_URL: String = "https://public-api.chiaexplorer.com/0.1/balance/"
    const val CHIA_EXPLORER_API_KEY_HEADER_NAME: String = "x-api-key"
    const val CHIA_EXPLORER_API_KEY: String = BuildConfig.CHIA_EXPLORER_API_KEY
}
