package ninja.bored.chiapublicaddressmonitor.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Query

data class WidgetSettingsAndData(
    @Embedded
    val widgetSettings: WidgetSettings?,

    @Embedded
    val widgetData: WidgetData?,

    @Embedded(prefix = "ase_")
    val addressSettings: AddressSettings?
)

@Dao
interface WidgetSettingsAndDataDao {
    @Query(
        """
        SELECT  wd.chiaAddress, 
                wd.chia_amount, 
                wd.update_date,
                ws.chia_address, 
                ws.widgetID, 
                ase.update_time AS ase_update_time, 
                ase.show_notification AS ase_show_notification, 
                ase.chia_address_synonym AS ase_chia_address_synonym, 
                ase.chiaAddress AS ase_chiaAddress
        FROM widget_data AS wd 
        LEFT JOIN widget_settings AS ws 
        ON ws.chia_address = wd.chiaAddress 
        LEFT JOIN address_settings AS ase
        on wd.chiaAddress = ase.chiaAddress
        GROUP BY wd.chiaAddress 
        ORDER by wd.chiaAddress
        """
    )
    fun loadAll(): LiveData<List<WidgetSettingsAndData>>
}
