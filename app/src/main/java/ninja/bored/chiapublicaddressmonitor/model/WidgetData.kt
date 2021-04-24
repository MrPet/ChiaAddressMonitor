package ninja.bored.chiapublicaddressmonitor.model

import androidx.room.*
import java.util.*


@Entity(tableName = "widget_data")
data class WidgetData(
    @PrimaryKey val chiaAddress: String,
    @ColumnInfo(name = "chia_amount") val chiaAmount: Double,
    @ColumnInfo(name = "update_date") val updateDate: Date
                     )

@Dao
interface WidgetDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUpdate(widgetData: WidgetData)

    @Query("SELECT * FROM widget_data")
    suspend fun getAll(): List<WidgetData>?

    @Delete
    suspend fun delete(widgetData: WidgetData)

    @Query("SELECT * FROM widget_data WHERE chiaAddress = :chiaAddress")
    suspend fun getByAddress(chiaAddress: String): WidgetData?
}