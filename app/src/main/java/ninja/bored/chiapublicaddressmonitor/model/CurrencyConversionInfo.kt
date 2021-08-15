package ninja.bored.chiapublicaddressmonitor.model

data class CurrencyConversionInfo(
    val currencyCode: String,
    val precision: String,
    val hardcodedMultiplier: Double?
)
