package ninja.bored.chiapublicaddressmonitor.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date

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
    abstract fun getWidgetSettingsDao(): WidgetSettingsDao
    abstract fun getWidgetDataDao(): WidgetDataDao
    abstract fun getWidgetSettingsAndDataDao(): WidgetSettingsAndDataDao

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
