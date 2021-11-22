package ninja.bored.chiapublicaddressmonitor.model

data class CoinInfo(
    val allTheBlocksCoinUrlShort: String,
    val coinCurrencySymbol: String,
    val coinDisplayName: String,
    val netBalanceDivider: Double,
)
