package ninja.bored.chiapublicaddressmonitor.helpers

import ninja.bored.chiapublicaddressmonitor.BuildConfig


class Constants {
    companion object {
        const val ADDRESS_EXTRA: String = "CHIA_ADDRESS"
        const val CHIA_ADDRESS_LENGTH: Int = 62
        const val CHIA_ADDRESS_PREFIX: String = "xch"
        const val SHORT_DATE_TIME_FORMAT: String = "EEE, HH:mm"
        const val BASE_API_URL: String = "https://public-api.chiaexplorer.com/0.1/balance/"
        const val CHIA_EXPLORER_API_KEY_HEADER_NAME: String = "x-api-key"
        const val CHIA_EXPLORER_API_KEY: String = BuildConfig.CHIA_EXPLORER_API_KEY

    }
}