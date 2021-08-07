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

object DbVersion {
    const val VERSION_3 = 3
    const val VERSION_4 = 4
    const val VERSION_5 = 5
    const val VERSION_6 = 6
}

@Database(
    entities = [WidgetSettings::class, WidgetData::class, AddressSettings::class, ChiaLatestConversion::class],
    version = DbVersion.VERSION_6
)
@TypeConverters(WidgetDatabaseConverter::class)
abstract class ChiaWidgetRoomsDatabase : RoomDatabase() {
    abstract fun getWidgetSettingsDao(): WidgetSettingsDao
    abstract fun getWidgetDataDao(): WidgetDataDao
    abstract fun getWidgetSettingsAndDataDao(): WidgetSettingsAndDataDao
    abstract fun getAddressSettingsDao(): AddressSettingsDao
    abstract fun getChiaLatestConversionDaoDao(): ChiaLatestConversionDao

    companion object {
        @Volatile
        private var INSTANCE: ChiaWidgetRoomsDatabase? = null
        fun getInstance(context: Context): ChiaWidgetRoomsDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    val MIGRATION_3_4 =
                        object : Migration(DbVersion.VERSION_3, DbVersion.VERSION_4) {
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
                    val MIGRATION_4_5 =
                        object : Migration(DbVersion.VERSION_4, DbVersion.VERSION_5) {
                            override fun migrate(database: SupportSQLiteDatabase) {
                                database.execSQL(
                                    """
                                        ALTER TABLE
                                        `address_settings`
                                        ADD COLUMN precision TEXT
                                    """
                                )
                            }
                        }
                    val MIGRATION_5_6 =
                        object : Migration(DbVersion.VERSION_5, DbVersion.VERSION_6) {
                            override fun migrate(database: SupportSQLiteDatabase) {
                                database.execSQL(
                                    """
                                        CREATE TABLE
                                        `chia_latest_conversion`
                                         (
                                            `priceCurrency` CHARACTER(10) NOT NULL,
                                            `price` REAL NOT NULL,
                                            `update_date` DATETIME  NOT NULL,
                                            `device_import_date` DATETIME  NOT NULL,
                                         PRIMARY KEY (`priceCurrency`))
                                        """
                                )
                                database.execSQL(
                                    """
                                          ALTER TABLE
                                        `conversion_currency`
                                        ADD COLUMN precision TEXT
                                        """
                                )
                            }
                        }

                    instance = Room.databaseBuilder(
                        context,
                        ChiaWidgetRoomsDatabase::class.java, "chia-address-widget-db"
                    )
                        .addMigrations(MIGRATION_3_4)
                        .addMigrations(MIGRATION_4_5)
                        .addMigrations(MIGRATION_5_6)
                        .build()
                }
                return instance
            }
        }
    }
}
