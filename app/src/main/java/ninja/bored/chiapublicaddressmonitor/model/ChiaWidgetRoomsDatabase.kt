package ninja.bored.chiapublicaddressmonitor.model

import android.content.Context
import androidx.room.*
import java.util.*

class WidgetDatabaseConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}


@Database(entities = [WidgetSettings::class, WidgetData::class], version = 3)
@TypeConverters(WidgetDatabaseConverter::class)
abstract class ChiaWidgetRoomsDatabase : RoomDatabase() {
    abstract fun WidgetSettingsDao(): WidgetSettingsDao
    abstract fun WidgetDataDao(): WidgetDataDao
    abstract fun WidgetSettingsAndDataDao(): WidgetSettingsAndDataDao

    companion object {
        @Volatile
        private var INSTANCE: ChiaWidgetRoomsDatabase? = null
        fun getInstance(context: Context): ChiaWidgetRoomsDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context,
                        ChiaWidgetRoomsDatabase::class.java, "chia-address-widget-db"
                                                   ).build()
                }
                return instance
            }
        }

    }

}

