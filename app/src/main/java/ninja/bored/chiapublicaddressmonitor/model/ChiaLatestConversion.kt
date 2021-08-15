package ninja.bored.chiapublicaddressmonitor.model

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import java.util.Date

@Entity(tableName = "chia_latest_conversion")
data class ChiaLatestConversion(
    @PrimaryKey val priceCurrency: String,
    @ColumnInfo(name = "price") val price: Double,
    @ColumnInfo(name = "update_date") val updateDate: Date,
    @ColumnInfo(name = "device_import_date") val deviceImportDate: Date
) {
    constructor(chiaConversionResponseData: ChiaConversionResponseData) : this(
        chiaConversionResponseData.priceCurrency,
        chiaConversionResponseData.price,
        chiaConversionResponseData.updateDateTime,
        Date()
    )
}

@Dao
interface ChiaLatestConversionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdate(chiaLatestConversion: ChiaLatestConversion)

    @Delete
    suspend fun delete(chiaLatestConversion: ChiaLatestConversion)

    @Query("""
                    SELECT * FROM chia_latest_conversion 
                    WHERE priceCurrency = :paraCurrency 
                """)
    suspend fun getLatestForCurrency(paraCurrency: String): ChiaLatestConversion?
}
