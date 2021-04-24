package ninja.bored.chiapublicaddressmonitor.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query

data class WidgetSettingsAndData(
    @Embedded
    val widgetSettings: WidgetSettings?,

    @Embedded
    val widgetData: WidgetData?
                                )

@Dao
interface WidgetSettingsAndDataDao {
    @Query("SELECT * FROM widget_data AS wd LEFT JOIN widget_settings AS ws  ON ws.chia_address = wd.chiaAddress GROUP BY wd.chiaAddress ORDER by wd.chiaAddress")
    fun loadAll(): LiveData<List<WidgetSettingsAndData>>
}