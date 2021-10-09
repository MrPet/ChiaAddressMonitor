package ninja.bored.chiapublicaddressmonitor.model

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction

@Entity(tableName = "widget_address_grouping_settings")
data class WidgetAddressGroupingSettings(
    @PrimaryKey val widgetID: Int,
    @ColumnInfo(name = "currency") val currency: String,
)

data class WidgetAddressGroupSettingsWithAddresses(
    @Embedded val widgetAddressGroupSettings: WidgetAddressGroupingSettings,
    @Relation(parentColumn = "widgetID", entityColumn = "widgetID")
    val widgetAddresses: List<WidgetAddressGroupingSettingsHasAddress>
) {
    suspend fun insertUpdate(database: ChiaWidgetRoomsDatabase) {
        val widgetAddressGroupingSettingsDao = database.getWidgetAddressGroupingSettingsDao()
        widgetAddressGroupingSettingsDao.insertUpdate(widgetAddressGroupSettings)
        val widgetAddressGroupingSettingsHasAddressDao =
            database.getWidgetAddressGroupingSettingsHasAddressDao()
        widgetAddressGroupingSettingsHasAddressDao.deleteWithWidgetID(widgetAddressGroupSettings.widgetID)
        widgetAddresses.forEach {
            widgetAddressGroupingSettingsHasAddressDao.insertUpdate(it)
        }
    }
}

@Dao
interface WidgetAddressGroupSettingsWithAddressesDao {
    @Transaction
    @Query("SELECT * FROM widget_address_grouping_settings WHERE widgetID = :widgetID")
    suspend fun getWidgetAddressGroupWithAddresses(widgetID: Int): WidgetAddressGroupSettingsWithAddresses?

    @Transaction
    @Query("SELECT * FROM widget_address_grouping_settings")
    suspend fun loadAll(): List<WidgetAddressGroupSettingsWithAddresses>?
}

@Dao
interface WidgetAddressGroupingSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdate(widgetSettings: WidgetAddressGroupingSettings)

    @Delete
    suspend fun delete(widgetSettings: WidgetAddressGroupingSettings)

    @Transaction
    @Query("SELECT * FROM widget_address_grouping_settings WHERE widgetID = :widgetID")
    suspend fun getByID(widgetID: Int): WidgetAddressGroupingSettings?
}

@Entity(
    tableName = "widget_address_grouping_settings_has_address",
    primaryKeys = ["chia_address", "widgetID"]
)
data class WidgetAddressGroupingSettingsHasAddress(
    @ColumnInfo(name = "widgetID") val widgetID: Int,
    @ColumnInfo(name = "chia_address") val chiaAddress: String
)

@Dao
interface WidgetAddressGroupingSettingsHasAddressDao {
    @Delete
    suspend fun delete(widgetAddressGroupSettingAddresses: WidgetAddressGroupingSettingsHasAddress)

    @Query("DELETE FROM widget_address_grouping_settings_has_address WHERE widgetID = :widgetID")
    suspend fun deleteWithWidgetID(widgetID: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdate(widgetAddressGroupSettingAddresses: WidgetAddressGroupingSettingsHasAddress)

    @Query("SELECT * FROM widget_address_grouping_settings_has_address WHERE widgetID = :widgetID")
    suspend fun getByWidgetID(widgetID: Int): List<WidgetAddressGroupingSettingsHasAddress>?
}
