package ninja.bored.chiapublicaddressmonitor.model

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "widget_fiat_conversion_settings")
data class WidgetFiatConversionSettings(
    @PrimaryKey val widgetID: Int,
    @ColumnInfo(name = "conversion_currency") val conversionCurrency: String
)

@Dao
interface WidgetFiatConversionSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdate(widgetFiatConversionSettings: WidgetFiatConversionSettings)

    @Delete
    suspend fun delete(widgetFiatConversionSettings: WidgetFiatConversionSettings)

    @Query("SELECT * FROM widget_fiat_conversion_settings WHERE widgetID = :widgetID")
    suspend fun getByID(widgetID: Int): WidgetFiatConversionSettings?

    @Query("SELECT * FROM widget_fiat_conversion_settings")
    suspend fun loadAll(): List<WidgetFiatConversionSettings>?
}
