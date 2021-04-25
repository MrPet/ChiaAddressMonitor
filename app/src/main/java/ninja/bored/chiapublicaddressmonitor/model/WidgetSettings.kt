package ninja.bored.chiapublicaddressmonitor.model

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "widget_settings")
data class WidgetSettings(
    @PrimaryKey val widgetID: Int,
    @ColumnInfo(name = "chia_address") val chiaAddress: String
)

@Dao
interface WidgetSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdate(widgetSettings: WidgetSettings)

    @Delete
    suspend fun delete(widgetSettings: WidgetSettings)

    @Query("SELECT * FROM widget_settings WHERE widgetID = :widgetID")
    suspend fun getByID(widgetID: Int): WidgetSettings?
}
