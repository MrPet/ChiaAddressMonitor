package ninja.bored.chiapublicaddressmonitor.model

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "address_settings")
data class AddressSettings(
    @PrimaryKey val chiaAddress: String,
    @ColumnInfo(name = "show_notification") val showNotification: Boolean,
    @ColumnInfo(name = "chia_address_synonym") val chiaAddressSynonym: String?,
    @ColumnInfo(name = "update_time") val updateTime: Int // in seconds
)

@Dao
interface AddressSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdate(addressSettings: AddressSettings)

    @Delete
    suspend fun delete(addressSettings: AddressSettings)

    @Query("SELECT * FROM address_settings WHERE chiaAddress = :chiaAddress")
    suspend fun getByAddress(chiaAddress: String): AddressSettings?
}
