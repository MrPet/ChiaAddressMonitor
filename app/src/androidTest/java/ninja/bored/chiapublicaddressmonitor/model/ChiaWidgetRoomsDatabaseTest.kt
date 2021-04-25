package ninja.bored.chiapublicaddressmonitor.model

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.Date

@RunWith(AndroidJUnit4::class)
class ChiaWidgetRoomsDatabaseTest : TestCase() {
    private lateinit var widgetDataDao: WidgetDataDao
    private lateinit var widgetSettingsDao: WidgetSettingsDao
    private lateinit var widgetSettingsAndDataDao: WidgetSettingsAndDataDao
    private lateinit var db: ChiaWidgetRoomsDatabase

    @Before
    public override fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, ChiaWidgetRoomsDatabase::class.java
        ).build()

        widgetDataDao = db.getWidgetDataDao()
        widgetSettingsDao = db.getWidgetSettingsDao()
        widgetSettingsAndDataDao = db.getWidgetSettingsAndDataDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeAndReadWidgetData() = runBlocking {
        db.clearAllTables()

        val testAddress = "xch1xntpeve5yjnadgjsyhc2szvjw07xt6mkv7d2v3qfvsvj097sywls7m6k2v"
        val testAmount = 100.00
        val testDate = Date()
        val widgetData = WidgetData(testAddress, testAmount, testDate)

        widgetDataDao.insertUpdate(widgetData)

        val savedWidgetData = widgetDataDao.getByAddress(testAddress)

        assertEquals(
            "After Inserting the returned data should be equal to the inserted",
            widgetData, savedWidgetData
        )
    }

    @Test
    fun writeAndReadUpdateWidgetData() = runBlocking {
        db.clearAllTables()

        val testAddress = "xch1xntpeve5yjnadgjsyhc2szvjw07xt6mkv7d2v3qfvsvj097sywls7m6k2v"
        val testAmount = 100.00
        val testDate = Date()
        val widgetData = WidgetData(testAddress, testAmount, testDate)

        widgetDataDao.insertUpdate(widgetData)

        val newTestAmount = 110.00
        val newTestDate = Date(testDate.time + 10000)

        val savedWidgetData = widgetDataDao.getByAddress(testAddress)
        assertEquals(
            "After Inserting the returned data should be equal to the inserted",
            widgetData, savedWidgetData
        )

        val newWidgetData = WidgetData(testAddress, newTestAmount, newTestDate)

        assertNotEquals("New WidgetData cant be the same as the old", widgetData, newWidgetData)

        widgetDataDao.insertUpdate(newWidgetData)
        val newSavedWidgetData = widgetDataDao.getByAddress(testAddress)

        assertEquals(
            "New loaded WidgetData must be the same als the updated",
            newWidgetData, newSavedWidgetData
        )
    }

    @Test
    fun writeAndDeleteWidgetData() = runBlocking {
        db.clearAllTables()

        val testAddress = "xch1xntpeve5yjnadgjsyhc2szvjw07xt6mkv7d2v3qfvsvj097sywls7m6k2v"
        val testAmount = 100.00
        val testDate = Date()
        val widgetData = WidgetData(testAddress, testAmount, testDate)

        widgetDataDao.insertUpdate(widgetData)

        val savedWidgetData = widgetDataDao.getByAddress(testAddress)
        assertEquals(
            "After Inserting the returned data should be equal to the inserted",
            savedWidgetData, widgetData
        )

        savedWidgetData?.let {
            widgetDataDao.delete(savedWidgetData)
        }

        val savedWidgetDataAfterDelete = widgetDataDao.getByAddress(testAddress)

        assertNull("After Delete no data should be returned", savedWidgetDataAfterDelete)
    }

    @Test
    fun writeAndReadWidgetSettings() = runBlocking {
        db.clearAllTables()

        val testAddress = "xch1xntpeve5yjnadgjsyhc2szvjw07xt6mkv7d2v3qfvsvj097sywls7m6k2v"
        val widgetID = 10
        val widgetSettings = WidgetSettings(widgetID, testAddress)

        widgetSettingsDao.insertUpdate(widgetSettings)

        val savedWidgetSettings = widgetSettingsDao.getByID(widgetID)
        assertEquals(
            "Saved WidgetSettings are the same as the inserted",
            savedWidgetSettings, widgetSettings
        )
    }

    @Test
    fun writeAndReadUpdateWidgetSettings() = runBlocking {
        db.clearAllTables()

        val testAddress = "xch1xntpeve5yjnadgjsyhc2szvjw07xt6mkv7d2v3qfvsvj097sywls7m6k2v"
        val widgetID = 10
        val widgetSettings = WidgetSettings(widgetID, testAddress)

        widgetSettingsDao.insertUpdate(widgetSettings)

        val savedWidgetSettings = widgetSettingsDao.getByID(widgetID)
        assertEquals(
            "Saved WidgetSettings are the same as the inserted",
            savedWidgetSettings, widgetSettings
        )

        val newAddress = "xch1xntpeve6yjnadgjsyhc2szvjw07xt6mkv7d2v3qfvsvj097sywls7m6k2v"
        val newWidgetSettings = WidgetSettings(widgetID, newAddress)

        assertNotEquals(
            "New WidgetSettings are not the same as the saved",
            newWidgetSettings, savedWidgetSettings
        )

        widgetSettingsDao.insertUpdate(newWidgetSettings)

        val newSavedWidgetSettings = widgetSettingsDao.getByID(widgetID)
        assertEquals(
            "New WidgetSettings are the same as the new saved",
            newSavedWidgetSettings, newWidgetSettings
        )
    }

    @Test
    fun writeAndReadDeleteWidgetSettings() = runBlocking {
        db.clearAllTables()

        val testAddress = "xch1xntpeve5yjnadgjsyhc2szvjw07xt6mkv7d2v3qfvsvj097sywls7m6k2v"
        val widgetID = 10
        val widgetSettings = WidgetSettings(widgetID, testAddress)

        widgetSettingsDao.insertUpdate(widgetSettings)

        val savedWidgetSettings = widgetSettingsDao.getByID(widgetID)
        assertEquals(
            "Saved WidgetSettings are the same as the inserted",
            savedWidgetSettings, widgetSettings
        )

        widgetSettingsDao.delete(widgetSettings)

        assertNull(
            "After delete we cannot find any widgetSettings",
            widgetSettingsDao.getByID(widgetID)
        )
    }
}
