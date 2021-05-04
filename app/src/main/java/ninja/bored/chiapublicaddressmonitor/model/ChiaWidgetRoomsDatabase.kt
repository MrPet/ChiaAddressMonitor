package ninja.bored.chiapublicaddressmonitor.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

@Database(
    entities = [WidgetSettings::class, WidgetData::class, AddressSettings::class],
    version = 4
)
@TypeConverters(WidgetDatabaseConverter::class)
abstract class ChiaWidgetRoomsDatabase : RoomDatabase() {
    abstract fun getWidgetSettingsDao(): WidgetSettingsDao
    abstract fun getWidgetDataDao(): WidgetDataDao
    abstract fun getWidgetSettingsAndDataDao(): WidgetSettingsAndDataDao
    abstract fun getAddressSettingsDao(): AddressSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: ChiaWidgetRoomsDatabase? = null
        fun getInstance(context: Context): ChiaWidgetRoomsDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    val MIGRATION_3_4 = object : Migration(3, 4) {
                        override fun migrate(database: SupportSQLiteDatabase) {
                            database.execSQL(
                                """
                                        CREATE TABLE
                                        `address_settings`
                                            (`chiaAddress` TEXT NOT NULL,
                                             `show_notification` INTEGER NOT NULL,
                                             `chia_address_synonym` TEXT,
                                             `update_time` INTEGER  NOT NULL,
                                        PRIMARY KEY (`chiaAddress`))
                                    """
                            )
                        }
                    }

                    instance = Room.databaseBuilder(
                        context,
                        ChiaWidgetRoomsDatabase::class.java, "chia-address-widget-db"
                    )
                        .addMigrations(MIGRATION_3_4)
                        .build()
                }
                return instance
            }
        }
    }
}
